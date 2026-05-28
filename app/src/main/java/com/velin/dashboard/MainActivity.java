package com.velin.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.*;
import android.graphics.Color;

public class MainActivity extends Activity {

    private WebView webView;
    private static final String BACKOFFICE = "https://backoffice-fredo-prod.apnl.info";
    private final Handler handler = new Handler();
    private String storedBikesJson = null;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.parseColor("#0d0f14"));
        getWindow().setNavigationBarColor(Color.parseColor("#0d0f14"));

        webView = new WebView(this);
        setContentView(webView);

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
                    handler.postDelayed(() -> tryInject(view, 0), 3000);
                } else if (url.contains("/client")) {
                    view.loadUrl(BACKOFFICE + "/clientZones/");
                }
            }
        });

        webView.loadUrl(BACKOFFICE + "/log-in");
    }

    private void tryInject(WebView view, int attempt) {
        if (attempt > 10) {
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
                    fetchRentalsViaFetch(view);
                } else {
                    handler.postDelayed(() -> tryInject(view, attempt + 1), 1000);
                }
            }
        );
    }

    private void fetchRentalsViaFetch(WebView view) {
        view.evaluateJavascript(
            "(function(){" +
            "  window.__RENTALS_READY__ = undefined;" +
            "  fetch('/clientHistory/unfinished', {" +
            "    credentials: 'include'," +
            "    headers: {'X-Requested-With': 'XMLHttpRequest'}" +
            "  })" +
            "  .then(function(r){ return r.text(); })" +
            "  .then(function(html){" +
            "    var parser = new DOMParser();" +
            "    var doc = parser.parseFromString(html, 'text/html');" +
            "    var active = [];" +
            "    doc.querySelectorAll('table tbody tr').forEach(function(row){" +
            "      var cells = row.querySelectorAll('td');" +
            "      if(cells.length >= 2){" +
            "        cells[1].innerText.trim().split('\\n').forEach(function(line){" +
            "          var t = line.trim();" +
            "          if(t.length > 0) active.push(t);" +
            "        });" +
            "      }" +
            "    });" +
            "    window.__RENTALS_READY__ = JSON.stringify(active);" +
            "  })" +
            "  .catch(function(e){" +
            "    window.__RENTALS_READY__ = '[]';" +
            "  });" +
            "})()",
            null
        );
        pollRentals(view, 0);
    }

    private void pollRentals(WebView view, int attempt) {
        if (attempt > 20) {
            injectDashboard(view, storedBikesJson, "[]");
            return;
        }
        handler.postDelayed(() -> {
            view.evaluateJavascript(
                "typeof window.__RENTALS_READY__ !== 'undefined' ? window.__RENTALS_READY__ : 'null'",
                result -> {
                    if (result != null && !result.equals("null") && !result.equals("\"null\"")) {
                        String cleanRentals = result.substring(1, result.length() - 1).replace("\\\"", "\"");
                        injectDashboard(view, storedBikesJson, cleanRentals);
                    } else {
                        pollRentals(view, attempt + 1);
                    }
                }
            );
        }, 500);
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
