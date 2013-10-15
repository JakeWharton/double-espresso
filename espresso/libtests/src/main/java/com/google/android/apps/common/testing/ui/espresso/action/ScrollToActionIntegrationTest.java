package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.ScrollActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

/**
 * Tests for ScrollToAction.
 */
@LargeTest
public class ScrollToActionIntegrationTest extends ActivityInstrumentationTestCase2<ScrollActivity>
{
  @SuppressWarnings("deprecation")
  public ScrollToActionIntegrationTest() {
    // Keep froyo happy.
    super("com.google.android.apps.common.testing.ui.testapp", ScrollActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testScrollDown() {
    onView(withId(is(R.id.bottom_left)))
      .check(matches(not(isDisplayed())))
      .perform(scrollTo())
      .check(matches(isDisplayed()))
      .perform(scrollTo()); // Should be a noop.
  }

  public void testScrollVerticalAndHorizontal() {
    onView(withId(is(R.id.bottom_right)))
      .check(matches(not(isDisplayed())))
      .perform(scrollTo())
      .check(matches(isDisplayed()));
    onView(withId(is(R.id.top_left)))
      .check(matches(not(isDisplayed())))
      .perform(scrollTo())
      .check(matches(isDisplayed()));
  }

  public void testScrollWithinScroll() {
    onView(withId(is(R.id.double_scroll)))
      .check(matches(not(isDisplayed())))
      .perform(scrollTo())
      .check(matches(isDisplayed()));
  }
}
