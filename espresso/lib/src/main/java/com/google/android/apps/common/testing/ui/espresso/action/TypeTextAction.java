package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasFocus;
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
  private final boolean tapToFocus;

  /**
   * Constructs {@link TypeTextAction} with given string. If the string is empty it results in no-op
   * (nothing is typed). By default this action sends a tap event to the center of the view to 
   * attain focus before typing.
   *
   * @param stringToBeTyped String To be typed by {@link TypeTextAction}
   */
  public TypeTextAction(String stringToBeTyped) {
    this(stringToBeTyped, true);
  }

  /**
   * Constructs {@link TypeTextAction} with given string. If the string is empty it results in no-op
   * (nothing is typed).
   *
   * @param stringToBeTyped String To be typed by {@link TypeTextAction}
   * @param tapToFocus indicates whether a tap should be sent to the underlying view before typing.
   */
  public TypeTextAction(String stringToBeTyped, boolean tapToFocus) {
    checkNotNull(stringToBeTyped);
    this.stringToBeTyped = stringToBeTyped;
    this.tapToFocus = tapToFocus;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    Matcher<View> matchers = allOf(isDisplayed());
    if (!tapToFocus) {
      matchers = allOf(matchers, hasFocus());
    }

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

    if (tapToFocus) {
      // Perform a click.
      new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER)
          .perform(uiController, view);
      uiController.loopMainThreadUntilIdle();
    }

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
