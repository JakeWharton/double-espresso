package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressImeActionButton;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasImeAction;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import com.google.android.apps.common.testing.ui.espresso.PerformException;
import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.inputmethod.EditorInfo;

/**
 * Tests for {@link EditorAction}.
 */
@LargeTest
public class EditorActionIntegrationTest extends ActivityInstrumentationTestCase2<SendActivity> {
  @SuppressWarnings("deprecation")
  public EditorActionIntegrationTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", SendActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  @SuppressWarnings("unchecked")
  public void testPressImeActionButtonOnSearchBox() {
    String searchFor = "rainbows and unicorns";
    onView(withId(R.id.search_box)).perform(scrollTo(), ViewActions.typeText(searchFor));
    onView(withId(R.id.search_box))
        .check(matches(hasImeAction(EditorInfo.IME_ACTION_SEARCH)))
        .perform(pressImeActionButton());
    onView(withId(R.id.search_result)).perform(scrollTo());
    onView(withId(R.id.search_result))
        .check(matches(allOf(isDisplayed(), withText(containsString(searchFor)))));
  }

  public void testPressImeActionButtonOnNonEditorWidget() {
    try {
      onView(withId(R.id.send_button)).perform(pressImeActionButton());
      fail("Expected exception on previous call");
    } catch (PerformException expected) {
      assertTrue(expected.getCause() instanceof IllegalStateException);
    }
  }

  public void testPressSearchOnDefaultEditText() {
    onView(withId(R.id.enter_data_edit_text)).perform(pressImeActionButton());
  }
}
