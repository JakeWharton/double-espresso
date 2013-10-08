package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;

import android.view.View;
import android.widget.EditText;

import org.hamcrest.Matcher;

/**
 * Clears view text by setting {@link EditText}s text property to "".
 */
public final class ClearTextAction implements ViewAction {
  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    return allOf(isDisplayed(), isAssignableFrom(EditText.class));
  }

  @Override
  public void perform(UiController uiController, View view) {
    ((EditText) view).setText("");
  }

  @Override
  public String getDescription() {
    return "clear text";
  }
}
