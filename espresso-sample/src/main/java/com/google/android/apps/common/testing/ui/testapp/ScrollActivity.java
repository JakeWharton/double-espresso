package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.os.Bundle;

/**
 * An activity displaying various scroll views.
 */
public class ScrollActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scroll_activity);
  }
}
