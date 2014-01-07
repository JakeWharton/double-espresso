package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.supportsInputMethods;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;
import com.google.android.apps.common.testing.ui.espresso.PerformException;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import org.hamcrest.Matcher;

/**
 * Enables typing text on views.
 */
public final class TypeTextAction implements ViewAction {
  private static final String TAG = TypeTextAction.class.getSimpleName();
  private final String stringToBeTyped;

  /**
   * Constructs {@link TypeTextAction} with given string. If the string is empty it results in no-op
   * (nothing is typed).
   *
   * @param stringToBeTyped String To be typed by {@link TypeTextAction}
   */
  public TypeTextAction(String stringToBeTyped) {
    checkNotNull(stringToBeTyped);
    this.stringToBeTyped = stringToBeTyped;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    Matcher<View> matchers = allOf(isDisplayed());
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
       return allOf(matchers, supportsInputMethods());
    } else {
       // SearchView does not support input methods itself (rather it delegates to an internal text
       // view for input).
       return allOf(matchers, anyOf(supportsInputMethods(), isAssignableFrom(SearchView.class)));
    }
  }

  @Override
  public void perform(UiController uiController, View view) {
    // No-op if string is empty.
    if (stringToBeTyped.length() == 0) {
      Log.w(TAG, "Supplied string is empty resulting in no-op (nothing is typed).");
      return;
    }

    // Perform a click.
    new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER)
        .perform(uiController, view);
    uiController.loopMainThreadUntilIdle();

    try {
      if (!uiController.injectString(stringToBeTyped)) {
        Log.e(TAG, "Failed to type text: " + stringToBeTyped);
        throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(new RuntimeException("Failed to type text: " + stringToBeTyped))
          .build();
      }
    } catch (InjectEventSecurityException e) {
      Log.e(TAG, "Failed to type text: " + stringToBeTyped);
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(e)
        .build();
    }
  }

  @Override
  public String getDescription() {
    return "type text";
  }
}
