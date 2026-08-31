package com.velin.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.*;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView webView;
    private View overlay;
    private static final String BACKOFFICE = "https://backoffice-fredo-prod.apnl.info";
    private final Handler handler = new Handler();
    private String storedBikesJson = null;
    private String storedRentalsJson = null;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.parseColor("#0d0f14"));
        getWindow().setNavigationBarColor(Color.parseColor("#0d0f14"));

        // FrameLayout principal
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Color.parseColor("#0d0f14"));

        // WebView
        webView = new WebView(this);
        frameLayout.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void showLoading() {
                showOverlay();
            }

            @android.webkit.JavascriptInterface
            public void refreshData() {
                runOnUiThread(() -> {
                    showOverlay();
                    startDataFetch(webView);
                });
            }
        }, "Android");

        // Overlay de chargement
        overlay = buildOverlay();
        frameLayout.addView(overlay, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(frameLayout);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/91.0.4472.120 Mobile Safari/537.36"
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(
                    "document.body.style.visibility='hidden';document.body.style.background='#0d0f14';",
                    null
                );

                if (url.contains("/log-in") || url.equals(BACKOFFICE + "/") || url.equals(BACKOFFICE)) {
                    view.evaluateJavascript(
                        "(function(){" +
                        "  var u=document.querySelector('input[name=_username],input[type=email]');" +
                        "  var p=document.querySelector('input[name=_password],input[type=password]');" +
                        "  var f=document.querySelector('form');" +
                        "  if(u&&p&&f){u.value='client+grandcalais@fredo.fr';p.value='Ic3nL6ciuAG7';f.submit();}" +
                        "})();",
                        null
                    );
                } else if (url.contains("/clientZones")) {
                    showOverlay();
                    handler.postDelayed(() -> startDataFetch(view), 400);
                } else if (url.contains("/client")) {
                    view.loadUrl(BACKOFFICE + "/clientZones/");
                }
            }
        });

        webView.loadUrl(BACKOFFICE + "/log-in");
    }

    private View buildOverlay() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#0d0f14"));
        layout.setGravity(android.view.Gravity.CENTER);

        // Titre
        TextView title = new TextView(this);
        title.setText("Fredo Stations");
        title.setTextColor(Color.parseColor("#f0f2f7"));
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        layout.addView(title);

        // Spinner
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
        params.topMargin = 40;
        params.gravity = android.view.Gravity.CENTER;
        spinner.setLayoutParams(params);
        layout.addView(spinner);

        return layout;
    }

    private void showOverlay() {
        runOnUiThread(() -> overlay.setVisibility(View.VISIBLE));
    }

    private void hideOverlay() {
        runOnUiThread(() -> overlay.setVisibility(View.GONE));
    }

    private void startDataFetch(WebView view) {
        storedBikesJson = null;
        storedRentalsJson = null;
        // Zones et locations sont récupérées en parallèle (indépendantes l'une de l'autre)
        injectRentalsFetchScript(view);
        pollRentals(view, 0);
        tryInject(view, 0);
    }

    private void maybeInjectDashboard(WebView view) {
        if (storedBikesJson != null && storedRentalsJson != null) {
            hideOverlay();
            String bikes = storedBikesJson;
            String rentals = storedRentalsJson;
            storedBikesJson = null;
            storedRentalsJson = null;
            injectDashboard(view, bikes, rentals);
        }
    }

    private void tryInject(WebView view, int attempt) {
        if (attempt > 20) {
            hideOverlay();
            view.evaluateJavascript(
                "document.body.style.visibility='visible';" +
                "document.body.innerHTML='<div style=\"color:red;padding:40px;font-family:sans-serif\">Erreur: données introuvables</div>';",
                null
            );
            return;
        }

        view.evaluateJavascript(
            "(function(){" +
            "  if(typeof namesByCoord === 'undefined' || typeof polygonsById === 'undefined') return 'null';" +
            "  var zones = {};" +
            "  Object.keys(polygonsById).forEach(function(id) {" +
            "    var poly = polygonsById[id];" +
            "    var path = poly.getPath().getArray().map(function(p){ return {lat:p.lat(),lng:p.lng()}; });" +
            "    zones[id] = {path:path};" +
            "  });" +
            "  return JSON.stringify({bikes: namesByCoord, zones: zones});" +
            "})()",
            value -> {
                if (value != null && !value.equals("null") && !value.equals("\"null\"")) {
                    storedBikesJson = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    maybeInjectDashboard(view);
                } else {
                    handler.postDelayed(() -> tryInject(view, attempt + 1), 300);
                }
            }
        );
    }

    private void injectRentalsFetchScript(WebView view) {
        view.evaluateJavascript(
            "(function(){" +
            "  window.__RENTALS_READY__ = undefined;" +
            "  function parseRentals(html, type){" +
            "    var parser = new DOMParser();" +
            "    var doc = parser.parseFromString(html, 'text/html');" +
            "    var active = [];" +
            "    doc.querySelectorAll('table tbody tr').forEach(function(row){" +
            "      var cells = row.querySelectorAll('td');" +
            "      if(cells.length >= 3){" +
            "        var station = '', dateHeure = '';" +
            "        if(type === 'maintenance'){" +
            "          dateHeure = cells[2].innerText.trim();" +
            "          var cell3 = cells.length > 3 ? cells[3].innerText.trim() : '';" +
            "          station = cell3.replace(/^(D\u00e9part|Depart)\\s*/i, '').trim() || 'Maintenance';" +
            "          if(dateHeure){" +
            "            var dp = dateHeure.split(' ');" +
            "            if(dp.length >= 2){" +
            "              var datePart = dp[0].replace(/(\\d{2})\\/(\\d{2})\\/\\d{2,4}/, '$1/$2');" +
            "              dateHeure = datePart + ' - ' + dp[1].substring(0,5);" +
            "            }" +
            "          }" +
            "          cells[1].innerText.trim().split('\\n').forEach(function(line){" +
            "            var t = line.trim();" +
            "            if(t.match(/^\\d{4,6}$/)){" +
            "              active.push({id: t, station: station, heure: dateHeure, type: type});" +
            "            }" +
            "          });" +
            "        } else {" +
            "          var cell2 = cells[2].innerText.trim();" +
            "          var parts = cell2.split('\\u2022');" +
            "          station = parts.length > 1 ? parts[1].trim() : '';" +
            "          dateHeure = parts.length > 0 ? parts[0].trim() : '';" +
            "          if(dateHeure){" +
            "            var dp = dateHeure.split(' ');" +
            "            if(dp.length >= 2){" +
            "              var datePart = dp[0].replace(/(\\d{2})\\/(\\d{2})\\/\\d{4}/, '$1/$2');" +
            "              dateHeure = datePart + ' - ' + dp[1];" +
            "            }" +
            "          }" +
            "          cells[1].innerText.trim().split('\\n').forEach(function(line){" +
            "            var t = line.trim();" +
            "            if(t.length === 4 && t.match(/^\\d{4}$/)){" +
            "              active.push({id: t, station: station, heure: dateHeure, type: type});" +
            "            }" +
            "          });" +
            "        }" +
            "      }" +
            "    });" +
            "    return active;" +
            "  }" +
            "  function parseText(text){" +
            "    try { var json = JSON.parse(text); return json.html || text; } catch(e) { return text; }" +
            "  }" +
            "  Promise.all([" +
            "    fetch('/clientHistory/unfinished', {credentials:'include', headers:{'X-Requested-With':'XMLHttpRequest'}}).then(function(r){return r.text();})" +
            "    .then(function(text){ return parseRentals(parseText(text), 'client'); })," +
            "    fetch('/clientMaintenanceHistory/unfinished', {credentials:'include', headers:{'X-Requested-With':'XMLHttpRequest'}}).then(function(r){return r.text();})" +
            "    .then(function(text){ return parseRentals(parseText(text), 'maintenance'); })" +
            "  ]).then(function(results){" +
            "    window.__RENTALS_READY__ = JSON.stringify(results[0].concat(results[1]));" +
            "  }).catch(function(){" +
            "    window.__RENTALS_READY__ = '[]';" +
            "  });" +
            "})()",
            null
        );
    }

    private void pollRentals(WebView view, int attempt) {
        if (attempt > 32) {
            if (storedRentalsJson == null) storedRentalsJson = "[]";
            maybeInjectDashboard(view);
            return;
        }
        handler.postDelayed(() -> {
            view.evaluateJavascript(
                "typeof window.__RENTALS_READY__ !== 'undefined' ? window.__RENTALS_READY__ : 'null'",
                result -> {
                    if (result != null && !result.equals("null") && !result.equals("\"null\"")) {
                        storedRentalsJson = result.substring(1, result.length() - 1).replace("\\\"", "\"");
                        maybeInjectDashboard(view);
                    } else {
                        pollRentals(view, attempt + 1);
                    }
                }
            );
        }, 250);
    }

    private void injectDashboard(WebView view, String bikesJson, String rentalsJson) {
        try {
            java.io.InputStream is = getAssets().open("dashboard.js");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String js = new String(buffer, "UTF-8");
            String init = "var __DATA__ = {b:" + bikesJson + ", r:" + rentalsJson + "}; " + js;
            view.evaluateJavascript(init, null);
        } catch (Exception e) {
            hideOverlay();
            view.evaluateJavascript(
                "document.body.style.visibility='visible';" +
                "document.body.innerHTML='<div style=\"color:red;padding:40px\">Erreur: " + e.getMessage() + "</div>';",
                null
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
