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
                } else if (url.contains("/clientHistory/unfinished")) {
                    handler.postDelayed(() -> extractRentals(view), 3000);
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
                    // Stocker les données vélos puis aller chercher les trajets en cours
                    String cleanJson = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    fetchActiveRentals(view, cleanJson);
                } else {
                    handler.postDelayed(() -> tryInject(view, attempt + 1), 1000);
                }
            }
        );
    }

    private void fetchActiveRentals(WebView view, String bikesJson) {
        // Stocker bikesJson pour l'utiliser après
        final String storedJson = bikesJson;
        // Naviguer vers la page des trajets en cours
        view.evaluateJavascript("window.__BIKES_JSON__ = '" + bikesJson.replace("'", "\\'") + "';", null);
        webView.loadUrl(BACKOFFICE + "/clientHistory/unfinished");
    }

    private void extractRentals(WebView view) {
        view.evaluateJavascript(
            "(function(){" +
            "  var active = [];" +
            "  document.querySelectorAll('table tbody tr').forEach(function(row){" +
            "    var cells = row.querySelectorAll('td');" +
            "    if(cells.length >= 2){" +
            "      cells[1].innerText.trim().split('\\n').forEach(function(line){" +
            "        var t = line.trim();" +
            "        if(t.length > 0) active.push(t);" +
            "      });" +
            "    }" +
            "  });" +
            "  return JSON.stringify(active);" +
            "})()",
            rentals -> {
                String cleanRentals = (rentals != null && !rentals.equals("null") && !rentals.equals("\"null\""))
                    ? rentals.substring(1, rentals.length() - 1).replace("\\\"", "\"")
                    : "[]";

                // Retourner sur la page zones
                webView.loadUrl(BACKOFFICE + "/clientZones/");

                handler.postDelayed(() -> {
                    view.evaluateJavascript(
                        "document.body.style.visibility='hidden';document.body.style.background='#0d0f14';",
                        null
                    );
                    // Récupérer le JSON des vélos stocké + injecter le dashboard
                    final String finalRentals = cleanRentals;
                    view.evaluateJavascript(
                        "(function(){ return typeof window.__BIKES_JSON__ !== 'undefined' ? window.__BIKES_JSON__ : 'null'; })()",
                        bikesJson -> {
                            if (bikesJson != null && !bikesJson.equals("null") && !bikesJson.equals("\"null\"")) {
                                String cleanBikes = bikesJson.substring(1, bikesJson.length() - 1).replace("\\\"", "\"");
                                injectDashboard(view, cleanBikes, finalRentals);
                            }
                        }
                    );
                }, 2000);
            }
        );
    }

    private void injectDashboard(WebView view, String bikesJson, String rentalsJson) {
        try {
            java.io.InputStream is = getAssets().open("dashboard.js");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String js = new String(buffer, "UTF-8");
            String init = "var __BIKES_DATA__ = " + bikesJson + "; var __ACTIVE_RENTALS__ = " + rentalsJson + "; " + js;
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
