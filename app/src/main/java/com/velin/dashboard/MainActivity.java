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
                    // Capturer les noms dès qu'ils apparaissent
                    captureZoneNames(view, 0);
                } else if (url.contains("/client")) {
                    view.loadUrl(BACKOFFICE + "/clientZones/");
                }
            }
        });

        webView.loadUrl(BACKOFFICE + "/log-in");
    }

    private void captureZoneNames(WebView view, int attempt) {
    view.evaluateJavascript(
        "(function(){" +
        "  return new Promise(function(resolve){" +
        "    function getNames(){" +
        "      var names = {};" +
        "      document.querySelectorAll('[data-zone-id]').forEach(function(li){" +
        "        var id = li.getAttribute('data-zone-id');" +
        "        var strong = li.querySelector('strong');" +
        "        var small = li.querySelector('small');" +
        "        if(strong && strong.innerText.trim()){" +
        "          var m = small ? small.innerText.match(/\\d+/) : null;" +
        "          names[id] = {nom: strong.innerText.trim(), places: m ? parseInt(m[0]) : 0};" +
        "        }" +
        "      });" +
        "      return names;" +
        "    }" +
        // Observer les changements du DOM
        "    var observer = new MutationObserver(function(){" +
        "      var n = getNames();" +
        "      if(Object.keys(n).length > 0){" +
        "        observer.disconnect();" +
        "        window.__ZONE_NAMES__ = n;" +
        "      }" +
        "    });" +
        "    observer.observe(document.body, {childList:true, subtree:true, characterData:true});" +
        // Vérifier aussi immédiatement
        "    var n = getNames();" +
        "    if(Object.keys(n).length > 0) window.__ZONE_NAMES__ = n;" +
        "  });" +
        "})()",
        null
    );

    // Attendre 3s puis lire __ZONE_NAMES__
    handler.postDelayed(() -> readCapturedNames(view, 0), 3000);
}

    private void readCapturedNames(WebView view, int attempt) {
        if (attempt > 10) {
            tryInjectWithNames(view, null, 0);
            return;
        }
        view.evaluateJavascript(
            "(function(){" +
            "  if(typeof window.__ZONE_NAMES__ !== 'undefined' && Object.keys(window.__ZONE_NAMES__).length > 0){" +
            "    return JSON.stringify(window.__ZONE_NAMES__);" +
            "  }" +
            "  return 'null';" +
            "})()",
            value -> {
                if (value != null && !value.equals("null") && !value.equals("\"null\"")) {
                    String cleanNames = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    tryInjectWithNames(view, cleanNames, 0);
                } else {
                    handler.postDelayed(() -> readCapturedNames(view, attempt + 1), 500);
                }
            }
        );
    }
    private void tryInjectWithNames(WebView view, String namesJson, int attempt) {
        if (attempt > 10) {
            view.evaluateJavascript(
                "document.body.style.visibility='visible';" +
                "document.body.innerHTML='<div style=\"color:red;padding:40px;font-family:sans-serif\">Erreur: données introuvables</div>';",
                null
            );
            return;
        }

        final String finalNames = namesJson != null ? namesJson : "{}";
        view.evaluateJavascript(
            "(function(){" +
            "  if(typeof namesByCoord === 'undefined' || typeof polygonsById === 'undefined') return 'null';" +
            "  var zones = {};" +
            "  var savedNames = " + finalNames + ";" +
            "  Object.keys(polygonsById).forEach(function(id) {" +
            "    var poly = polygonsById[id];" +
            "    var path = poly.getPath().getArray().map(function(p){ return {lat:p.lat(),lng:p.lng()}; });" +
            "    var saved = savedNames[id];" +
            "    var nom = saved ? saved.nom : 'Zone '+id;" +
            "    var places = saved ? saved.places : 0;" +
            "    zones[id] = {nom:nom, places:places, path:path};" +
            "  });" +
            "  return JSON.stringify({bikes: namesByCoord, zones: zones});" +
            "})()",
            value -> {
                if (value != null && !value.equals("null") && !value.equals("\"null\"")) {
                    String cleanJson = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    injectDashboard(view, cleanJson);
                } else {
                    handler.postDelayed(() -> tryInjectWithNames(view, namesJson, attempt + 1), 1000);
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
