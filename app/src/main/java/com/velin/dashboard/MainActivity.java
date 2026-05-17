package com.velin.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.graphics.Color;

public class MainActivity extends Activity {

    private WebView webView;
    private static final String BACKOFFICE = "https://backoffice-fredo-prod.apnl.info";

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
                if (url.contains("/log-in") || url.equals(BACKOFFICE + "/") || url.equals(BACKOFFICE)) {
                    // Auto-login : remplir et soumettre le formulaire
                    view.evaluateJavascript(
                        "(function(){" +
                        "  var u = document.querySelector('input[name=_username], input[type=email], input[name=email]');" +
                        "  var p = document.querySelector('input[name=_password], input[type=password]');" +
                        "  var f = document.querySelector('form');" +
                        "  if(u && p && f){" +
                        "    u.value='client+grandcalais@fredo.fr';" +
                        "    p.value='Ic3nL6ciuAG7';" +
                        "    f.submit();" +
                        "  } else {" +
                        "    document.body.style.background='#0d0f14';" +
                        "    document.body.innerHTML='<p style=\"color:red;padding:20px\">Formulaire non trouvé: '+document.body.innerHTML.substring(0,200)+'</p>';" +
                        "  }" +
                        "})();",
                        null
                    );
                } else if (url.contains("/clientZones")) {
                    // On est sur la bonne page, injecter le dashboard
                    injectDashboard(view);
                } else if (url.contains("/client")) {
                    // Connecté mais pas sur zones, naviguer vers zones
                    view.loadUrl(BACKOFFICE + "/clientZones/");
                }
            }
        });

        // Démarrer sur la page de login
        webView.loadUrl(BACKOFFICE + "/log-in");
    }

    private void injectDashboard(WebView view) {
        // Lire namesByCoord et injecter le dashboard
        view.evaluateJavascript(
            "(function(){" +
            "  if(typeof namesByCoord !== 'undefined'){" +
            "    return JSON.stringify(namesByCoord);" +
            "  }" +
            "  return 'null';" +
            "})();",
            value -> {
                if (value != null && !value.equals("null") && !value.equals("\"null\"")) {
                    // Charger le dashboard avec les données
                    String bikes = value.replace("'", "\\'");
                    view.loadUrl(BACKOFFICE + "/clientZones/");
                    view.evaluateJavascript(
                        "window.__BIKES__ = " + value.substring(1, value.length()-1).replace("\\\"", "\"") + ";",
                        null
                    );
                    loadDashboardFile(view, value);
                } else {
                    view.evaluateJavascript(
                        "document.body.innerHTML='<p style=\"color:orange;padding:20px;font-family:sans-serif\">namesByCoord non trouvé sur ' + window.location.href + '</p>';",
                        null
                    );
                }
            }
        );
    }

    private void loadDashboardFile(WebView view, String bikesJson) {
        try {
            java.io.InputStream is = getAssets().open("dashboard.js");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String js = new String(buffer, "UTF-8");
            // Passer les données au dashboard
            String init = "var __BIKES_DATA__ = " + 
                bikesJson.substring(1, bikesJson.length()-1).replace("\\\"", "\"") + 
                "; " + js;
            view.evaluateJavascript(init, null);
        } catch (Exception e) {
            view.evaluateJavascript(
                "document.body.innerHTML='<p style=\"color:red;padding:20px\">Erreur: " + e.getMessage() + "</p>';",
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
