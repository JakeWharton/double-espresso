package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

/**
 * {@link ClearTextAction} integration tests.
 */
@LargeTest
public class ClearTextActionIntegrationTest extends ActivityInstrumentationTestCase2<SendActivity> {
  @SuppressWarnings("deprecation")
  public ClearTextActionIntegrationTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", SendActivity.class);
  }

  @LargeTest
  public void testClearTextActionPerform() {
    Activity activity = getActivity();
    String text = activity.getText(R.string.send_data_to_message_edit_text).toString();
    onView(withId(is(R.id.send_data_to_message_edit_text))).check(matches(withText(is(text))));
    onView(withId(is(R.id.send_data_to_message_edit_text))).perform(clearText());
    onView(withId(is(R.id.send_data_to_message_edit_text))).check(matches(withText(is(""))));
  }

  @LargeTest
  public void testClearTextActionPerformWithTypeText() {
    Activity activity = getActivity();
    String text = activity.getText(R.string.send_data_to_message_edit_text).toString();
    onView(withId(is(R.id.send_data_to_call_edit_text))).perform(typeText(text));
    onView(withId(is(R.id.send_data_to_call_edit_text))).check(matches(withText(is(text))));
    onView(withId(is(R.id.send_data_to_call_edit_text))).perform(clearText());
    onView(withId(is(R.id.send_data_to_call_edit_text))).check(matches(withText(is(""))));
  }
}
