package com.google.android.apps.common.testing.ui.espresso;

import android.view.View;

import org.hamcrest.Matcher;



/**
 * Handles failures that happen during test execution.
 */
public interface FailureHandler {

  /**
   * Handle the given error in a manner that makes sense to the environment in which the test is
   * executed (e.g. take a screenshot, output extra debug info, etc). Upon handling, most handlers
   * will choose to propagate the error.
   */
  public void handle(Throwable error, Matcher<View> viewMatcher);

}
