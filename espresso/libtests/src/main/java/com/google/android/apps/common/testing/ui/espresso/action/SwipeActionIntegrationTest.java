package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeLeft;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasDescendant;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SwipeActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

/**
 * Integration tests for swiping actions.
 */
@LargeTest
public class SwipeActionIntegrationTest extends ActivityInstrumentationTestCase2<SwipeActivity> {

  @SuppressWarnings("deprecation")
  public SwipeActionIntegrationTest() {
    // Keep froyo happy.
    super("com.google.android.apps.common.testing.ui.testapp", SwipeActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  /** Tests that a small view can be swiped in both directions. */
  public void testSwipeOverSmallView() {
    onView(withId(R.id.small_pager))
      .check(matches(hasDescendant(withText("Position #0"))))
      .perform(swipeLeft())
      .check(matches(hasDescendant(withText("Position #1"))))
      .perform(swipeLeft())
      .check(matches(hasDescendant(withText("Position #2"))))
      .perform(swipeRight())
      .check(matches(hasDescendant(withText("Position #1"))))
      .perform(swipeRight())
      .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe beyond the start of a view pager has no effect. */
  public void testSwipingRightHasNoEffectWhenAtStart() {
    onView(withId(R.id.small_pager))
      .check(matches(hasDescendant(withText("Position #0"))))
      .perform(swipeRight())
      .check(matches(hasDescendant(withText("Position #0"))))
      .perform(swipeRight())
      .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe beyond the end of a view pager has no effect. */
  public void testSwipingLeftHasNoEffectWhenAtEnd() {
    onView(withId(R.id.small_pager))
      .perform(swipeLeft())
      .perform(swipeLeft())
      .check(matches(hasDescendant(withText("Position #2"))))
      .perform(swipeLeft())
      .check(matches(hasDescendant(withText("Position #2"))))
      .perform(swipeLeft())
      .check(matches(hasDescendant(withText("Position #2"))));
  }

  /** Tests that swiping across a partially overlapped view works correctly. */
  public void testSwipeOverPartiallyOverlappedView() {
    onView(withId(R.id.overlapped_pager))
      .check(matches(hasDescendant(withText("Position #0"))))
      .perform(swipeLeft())
      .check(matches(hasDescendant(withText("Position #1"))))
      .perform(swipeRight())
      .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe a view that doesn't respond to swipes has no effect. */
  @SuppressWarnings("unchecked")
  public void testSwipeOverUnswipableView() {
    onView(withId(R.id.text_simple))
      .check(matches(allOf(isDisplayed(), withText(R.string.text_simple))))
      .perform(swipeLeft())
      .check(matches(allOf(isDisplayed(), withText(R.string.text_simple))))
      .perform(swipeRight())
      .check(matches(allOf(isDisplayed(), withText(R.string.text_simple))));
  }

}
