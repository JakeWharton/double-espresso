package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

/**
 * Activity to test swipe interactions.
 */
public class SwipeActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.swipe_activity);

    ((ViewPager) findViewById(R.id.small_pager)).setAdapter(new SimplePagerAdapter());
    ((ViewPager) findViewById(R.id.overlapped_pager)).setAdapter(new SimplePagerAdapter());
  }

}

