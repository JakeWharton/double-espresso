package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.doesNotExist;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

/**
 * Ensures view root ordering works properly.
 */
@LargeTest
public class WindowOrderingIntegrationTest extends ActivityInstrumentationTestCase2<SendActivity> {
  @SuppressWarnings("deprecation")
  public WindowOrderingIntegrationTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", SendActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testPopupMenu() {
    if (Build.VERSION.SDK_INT < 11) {
      // popup menus are post honeycomb.
      return;
    }
    onView(withText(R.string.item_1_text))
        .check(doesNotExist());
    onView(withId(R.id.make_popup_menu_button))
        .perform(scrollTo(), click());
    onView(withText(R.string.item_1_text))
        .check(matches(isDisplayed()))
        .perform(click());
    onView(withText(R.string.item_1_text))
        .check(doesNotExist());
  }

  public void testPopupWindow() {
    getActivity();
    onView(withId(R.id.popup_title))
        .check(doesNotExist());
    onView(withId(R.id.make_popup_view_button))
        .perform(scrollTo(), click());
    onView(withId(R.id.popup_title))
        .check(matches(withText(R.string.popup_title)))
        .perform(click());
    onView(withId(R.id.popup_title))
        .check(doesNotExist());
  }

  public void testDialog() {
    onView(withText(R.string.dialog_title))
        .check(doesNotExist());
    onView(withId(R.id.make_alert_dialog))
        .perform(scrollTo(), click());
    onView(withText(R.string.dialog_title))
        .check(matches(isDisplayed()));

    onView(withText("Fine"))
        .perform(click());

    onView(withText(R.string.dialog_title))
        .check(doesNotExist());
  }
}
