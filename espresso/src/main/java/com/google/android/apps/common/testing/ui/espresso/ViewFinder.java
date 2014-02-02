package com.google.android.apps.common.testing.ui.espresso;

import android.view.View;

/**
 * Uses matchers to locate particular views within the view hierarchy.
 */
public interface ViewFinder {

  /**
   * Immediately locates a single view within the provided view hierarchy.
   *
   * If multiple views match, or if no views match the appropriate exception is thrown.
   *
   * @return A singular view which matches the matcher we were constructed with.
   * @throws AmbiguousViewMatcherException when multiple views match
   * @throws NoMatchingViewException when no views match.
   */
  public View getView() throws AmbiguousViewMatcherException, NoMatchingViewException;
}
