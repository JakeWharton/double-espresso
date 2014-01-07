package com.google.android.apps.common.testing.ui.espresso;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openContextualActionModeOverflowMenu;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.instanceOf;

import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.google.android.apps.common.testing.ui.testapp.ActionBarTestActivity;
import com.google.android.apps.common.testing.ui.testapp.MainActivity;
import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.hamcrest.Matcher;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests Espresso top level (i.e. ones not specific to a view) actions like pressBack and
 * closeSoftKeyboard.
 */
@LargeTest
public class EspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
  @SuppressWarnings("deprecation")
  public EspressoTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", MainActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  @SuppressWarnings("unchecked")
  public void testOpenOverflowInActionMode() {
    onData(allOf(instanceOf(Map.class), hasValue(ActionBarTestActivity.class.getSimpleName())))
        .perform(click());
    openContextualActionModeOverflowMenu();
    onView(withText("Key"))
        .perform(click());
    onView(withId(R.id.text_action_bar_result))
        .check(matches(withText("Key")));
  }

  @SuppressWarnings("unchecked")
  public void testOpenOverflowFromActionBar() {
    onData(allOf(instanceOf(Map.class), hasValue(ActionBarTestActivity.class.getSimpleName())))
        .perform(click());
    onView(withId(R.id.hide_contextual_action_bar))
        .perform(click());
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    onView(withText("World"))
        .perform(click());
    onView(withId(R.id.text_action_bar_result))
        .check(matches(withText("World")));
  }

  @SuppressWarnings("unchecked")
  public void testCloseSoftKeyboard() {
    onData(allOf(instanceOf(Map.class), hasValue(SendActivity.class.getSimpleName())))
        .perform(click());

    onView(withId(R.id.enter_data_edit_text)).perform(new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return anything();
      }

      @Override
      public void perform(UiController uiController, View view) {
        InputMethodManager imm = (InputMethodManager) getInstrumentation().getTargetContext()
          .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
        uiController.loopMainThreadUntilIdle();
      }

      @Override
      public String getDescription() {
        return "show soft input";
      }
    });

    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.closeSoftKeyboard());
  }

  public void testSetFailureHandler() {
    final AtomicBoolean handled = new AtomicBoolean(false);
    Espresso.setFailureHandler(new FailureHandler() {
      @Override
      public void handle(Throwable error, Matcher<View> viewMatcher) {
        handled.set(true);
      }
    });
    onView(withText("does not exist")).perform(click());
    assertTrue(handled.get());
  }

  public void testRegisterResourceWithNullName() {
    try {
      Espresso.registerIdlingResources(new IdlingResource() {
        @Override
        public boolean isIdleNow() {
          return true;
        }

        @Override
        public String getName() {
          return null;
        }

       @Override
       public void registerIdleTransitionCallback(ResourceCallback callback) {
         // ignore
       }
      });
      fail("Should have thrown NPE");
    } catch (NullPointerException expected) {}
  }
}
