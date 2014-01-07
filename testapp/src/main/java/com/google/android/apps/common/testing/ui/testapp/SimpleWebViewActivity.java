package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * One big web view to play with.
 */
public class SimpleWebViewActivity extends Activity {
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    WebView mainWebView = new WebView(this);
    setContentView(mainWebView);
    mainWebView.loadData("<html><body>Hello World <b> Enjoy! </b> </body></html>",
        "text/html", null);
    WebSettings settings = mainWebView.getSettings();
    settings.setJavaScriptEnabled(true);
  }
}
