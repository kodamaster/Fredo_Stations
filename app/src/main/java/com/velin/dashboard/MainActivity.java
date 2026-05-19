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
                // Cacher la page pendant le traitement
                view.evaluateJavascript(
                    "document.body.style.visibility='hidden';document.body.style.background='#0d0f14';",
                    null
                );
                // Ensuite attendre 4s avant d'injecter
                handler.postDelayed(() -> tryInject(view, 0), 7000);

                if (url.contains("/log-in") || url.equals(BACKOFFICE + "/") || url.equals(BACKOFFICE)) {
                    // Auto-login
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
                    // Attendre 2s que le JS de la page charge namesByCoord
                    handler.postDelayed(() -> tryInject(view, 0), 2000);
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
                "document.body.innerHTML='<div style=\"color:red;padding:40px\">Erreur: données introuvables</div>';",
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
            // Lire nom et places depuis les attributs data du li
            "    var li = document.querySelector('[data-zone-id=\"'+id+'\"]');" +
            "    var nom = 'Zone '+id;" +
            "    var places = 0;" +
            "    if(li){" +
            "      nom = li.dataset.zoneName || li.getAttribute('data-zone-name') || li.dataset.name || '';" +
            "      places = parseInt(li.dataset.zonePlaces || li.getAttribute('data-zone-places') || li.dataset.places || '0');" +
            // Si toujours vide, lire le texte brut du li
            "      if(!nom){" +
            "        var txt = li.textContent.trim();" +
            "        var lines = txt.split('\\n').map(function(l){return l.trim();}).filter(function(l){return l.length>0;});" +
            "        if(lines.length>0) nom = lines[0];" +
            "        if(lines.length>1){ var m=lines[1].match(/\\d+/); if(m) places=parseInt(m[0]); }" +
            "      }" +
            "    }" +
            "    zones[id] = {nom:nom, places:places, path:path};" +
            "  });" +
            "  return JSON.stringify({bikes: namesByCoord, zones: zones});" +
            "})()",
            value -> {
                if (value != null && !value.equals("null") && !value.equals("\"null\"")) {
                    String cleanJson = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    injectDashboard(view, cleanJson);
                } else {
                    handler.postDelayed(() -> tryInject(view, attempt + 1), 1000);
                }
            }
        );
    }

    private void injectDashboard(WebView view, String bikesJson) {
        try {
            java.io.InputStream is = getAssets().open("dashboard.js");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String js = new String(buffer, "UTF-8");
            String init = "var __BIKES_DATA__ = " + bikesJson + "; " + js;
            view.evaluateJavascript(init, null);
        } catch (Exception e) {
            view.evaluateJavascript(
                "document.body.style.visibility='visible';" +
                "document.body.innerHTML='<div style=\"color:red;padding:40px\">Erreur dashboard: " + e.getMessage() + "</div>';",
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
