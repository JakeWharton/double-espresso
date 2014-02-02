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
    mainWebView.loadData(
        "<html>" +
        "<script>document.was_clicked = false</script>" +
        "<body> " +
        "<button style='height:1000px;width:1000px;' onclick='document.was_clicked = true'> " +
        "I'm a button</button>" +
        "</body> " +
        "</html>", "text/html", null);
    WebSettings settings = mainWebView.getSettings();
    settings.setJavaScriptEnabled(true);
  }
}
