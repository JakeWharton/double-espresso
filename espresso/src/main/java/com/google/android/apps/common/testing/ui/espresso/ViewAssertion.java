package com.google.android.apps.common.testing.ui.espresso;

import com.google.common.base.Optional;

import android.view.View;


/**
 * Responsible for performing assertions on a View element.<br>
 * <p>
 * This is considered part of the test framework public API - developers are free to write their own
 * assertions as long as they meet the following requirements:
 * <ul>
 * <li>Do not mutate the passed in view.
 * <li>Throw junit.framework.AssertionError when the view assertion does not hold.
 * <li>Implementation runs on the UI thread - so it should not do any blocking operations
 * <li>Downcasting the view to a specific type is allowed, provided there is a test that view is an
 * instance of that type before downcasting. If not, an AssertionError should be thrown.
 * <li>It is encouraged to access non-mutating methods on the view to perform assertion.
 * </ul>
 * <br>
 * <p>
 * Strongly consider using a existing ViewAssertion via the ViewAssertions utility class before
 * writing your own assertion.
 */
public interface ViewAssertion {

  /**
   * Checks the state of the given view (if such a view is present).
   *
   * @param view the view, if one was found
   * @param noViewFoundException an exception detailing why the view could not be found.
   */
  void check(Optional<View> view, Optional<NoMatchingViewException> noViewFoundException);
}
