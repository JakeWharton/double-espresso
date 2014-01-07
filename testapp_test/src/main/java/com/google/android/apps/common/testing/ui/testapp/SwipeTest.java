package com.google.android.apps.common.testing.ui.espresso.sample;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeLeft;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.ViewPagerActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

/**
 * Demonstrates use of {@link ViewActions#swipeLeft()} and {@link ViewActions#swipeRight()}.
 */
@LargeTest
public class SwipeTest extends ActivityInstrumentationTestCase2<ViewPagerActivity> {

  @SuppressWarnings("deprecation")
  public SwipeTest() {
    // This constructor was deprecated - but we want to support lower API levels.
    super("com.google.android.apps.common.testing.ui.testapp", ViewPagerActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testSwipingThroughViews() {
    // Should be on position 0 to start with.
    onView(withText("Position #0")).check(matches(isDisplayed()));

    // Swipe left once.
    onView(withId(R.id.pager_layout)).perform(swipeLeft());

    // Now position 1 should be visible.
    onView(withText("Position #1")).check(matches(isDisplayed()));

    // Swipe left again.
    onView(withId(R.id.pager_layout)).perform(swipeLeft());

    // Now position 2 should be visible.
    onView(withText("Position #2")).check(matches(isDisplayed()));

    // Swipe left again.
    onView(withId(R.id.pager_layout)).perform(swipeLeft());

    // Position 2 should still be visible as this is the last view in the pager.
    onView(withText("Position #2")).check(matches(isDisplayed()));
  }

  public void testSwipingBackAndForward() {
    // Should be on position 0 to start with.
    onView(withText("Position #0")).check(matches(isDisplayed()));
    
    // Swipe left once.
    onView(withId(R.id.pager_layout)).perform(swipeLeft());
    
    // Now position 1 should be visible.
    onView(withText("Position #1")).check(matches(isDisplayed()));
    
    // Swipe back to the right.
    onView(withId(R.id.pager_layout)).perform(swipeRight());
    
    // Now position 0 should be visible again.
    onView(withText("Position #0")).check(matches(isDisplayed()));

    // Swipe right again.
    onView(withId(R.id.pager_layout)).perform(swipeRight());
    
    // Position 0 should still be visible as this is the first view in the pager.
    onView(withText("Position #0")).check(matches(isDisplayed()));
  }

}
