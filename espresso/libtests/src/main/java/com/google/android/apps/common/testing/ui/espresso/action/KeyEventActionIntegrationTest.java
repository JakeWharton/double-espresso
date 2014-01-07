package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isRoot;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.instanceOf;

import com.google.android.apps.common.testing.testrunner.annotations.SdkSuppress;
import com.google.android.apps.common.testing.ui.espresso.NoActivityResumedException;
import com.google.android.apps.common.testing.ui.testapp.MainActivity;
import com.google.android.apps.common.testing.ui.testapp.R;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.FlakyTest;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.Map;


/**
 * Integration tests for {@link KeyEventAction}.
 */
@LargeTest
public class KeyEventActionIntegrationTest extends ActivityInstrumentationTestCase2<MainActivity> {
  @SuppressWarnings("deprecation")
  public KeyEventActionIntegrationTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", MainActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public void testClickBackOnRootAction() {
    getActivity();
    try {
      pressBack();
      fail("Should have thrown NoActivityResumedException");
    } catch (NoActivityResumedException expected) {
    }
  }

  @SuppressWarnings("unchecked")
  public void testClickBackOnNonRootActivityLatte() {
    getActivity();
    onData(allOf(instanceOf(Map.class), hasValue("SendActivity"))).perform(click());
    pressBack();

    // Make sure we are back.
    onData(allOf(instanceOf(Map.class), hasValue("SendActivity"))).check(matches(isDisplayed()));
  }

  @SuppressWarnings("unchecked")
  public void testClickBackOnNonRootActionNoLatte() {
    getActivity();
    onData(allOf(instanceOf(Map.class), hasValue("SendActivity"))).perform(click());
    onView(isRoot()).perform(ViewActions.pressBack());

    // Make sure we are back.
    onData(allOf(instanceOf(Map.class), hasValue("SendActivity"))).check(matches(isDisplayed()));
  }

  @SuppressWarnings("unchecked")
  @SdkSuppress(versions = {7, 8, 10}, bugId = -1) // uses native fragments.
  @FlakyTest
  public void testClickOnBackFromFragment() {
    Intent fragmentStack = new Intent().setClassName(getInstrumentation().getTargetContext(),
        "com.google.android.apps.common.testing.ui.testapp.FragmentStack");
    fragmentStack.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getInstrumentation().startActivitySync(fragmentStack);
    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#1"))));
    try {
      pressBack();
      fail("Should have thrown NoActivityResumedException");
    } catch (NoActivityResumedException expected) {
    }
    getInstrumentation().startActivitySync(fragmentStack);

    onView(withId(R.id.new_fragment)).perform(click()).perform(click()).perform(click());

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#4"))));

    pressBack();

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#3"))));

    pressBack();

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#2"))));

    pressBack();

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#1"))));

    try {
      pressBack();
      fail("Should have thrown NoActivityResumedException");
    } catch (NoActivityResumedException expected) {
    }
  }

  @SuppressWarnings("unchecked")
  public void testPressKeyWithKeyCode() {
    getActivity();
    onData(allOf(instanceOf(Map.class), hasValue("SendActivity"))).perform(click());
    onView(withId(R.id.enter_data_edit_text)).perform(click());
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_X));
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_Y));
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_Z));
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER));
    onView(allOf(withId(R.id.enter_data_response_text), withText("xyz")))
        .check(matches(isDisplayed()));
  }
}
