package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.doubleClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.android.apps.common.testing.testrunner.annotations.SdkSuppress;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;
import com.google.android.apps.common.testing.ui.testapp.GestureActivity;
import com.google.android.apps.common.testing.ui.testapp.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Matcher;

/**
 * UI tests for ClickAction, LongClickAction and DoubleClickAction.
 */
@LargeTest
public class EventActionIntegrationTest extends ActivityInstrumentationTestCase2<GestureActivity> {

  @SuppressWarnings("deprecation")
  public EventActionIntegrationTest() {
    // Keep froyo happy.
    super("com.google.android.apps.common.testing.ui.testapp", GestureActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testClick() {
    onView(withText(is(getActivity().getString(R.string.text_click))))
        .check(matches(not(isDisplayed())));
    onView(withId(is(R.id.gesture_area))).perform(click());
    onView(withId(is(R.id.text_click))).check(matches(isDisplayed()));
    onView(withText(is(getActivity().getString(R.string.text_click))))
        .check(matches(isDisplayed()));
  }

  public void testBadClick() {
    onView(withText(is(getActivity().getString(R.string.text_click))))
        .check(matches(not(isDisplayed())));
    getActivity().setTouchDelay(700);

    onView(withId(is(R.id.gesture_area))).perform(click(
        new ViewAction() {
          @Override
          public String getDescription() {
            return "Handle tap->longclick.";
          }
          @Override
          public Matcher<View> getConstraints() {
            return isAssignableFrom(View.class);
          }
          @Override
          public void perform(UiController uiController, View view) {
            getActivity().setTouchDelay(0);
          }
        }));


    onView(withId(is(R.id.text_click))).check(matches(isDisplayed()));
    onView(withText(is(getActivity().getString(R.string.text_click))))
        .check(matches(isDisplayed()));
  }

  @SdkSuppress(bugId = -1, versions = {7, 8, 13})
  public void testLongClick() {
    onView(withText(is(getActivity().getString(R.string.text_long_click))))
        .check(matches(not(isDisplayed())));
    onView(withId(is(R.id.gesture_area))).perform(longClick());
    onView(withId(is(R.id.text_long_click))).check(matches(isDisplayed()));
    onView(withText(is(getActivity().getString(R.string.text_long_click))))
        .check(matches(isDisplayed()));
  }

  @SdkSuppress(bugId = -1, versions = {7, 8, 13})
  public void testDoubleClick() {
    onView(withText(is(getActivity().getString(R.string.text_double_click))))
        .check(matches(not(ViewMatchers.isDisplayed())));
    onView(withId(is(R.id.gesture_area))).perform(doubleClick());
    onView(withId(is(R.id.text_double_click))).check(matches(isDisplayed()));
    onView(withText(is("Double Click"))).check(matches(isDisplayed()));
    onView(withText(is(getActivity().getString(R.string.text_double_click))))
        .check(matches(isDisplayed()));
  }
}
