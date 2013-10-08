package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;

import com.google.android.apps.common.testing.ui.espresso.PerformException;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.hamcrest.Matcher;

/**
 * Performs whatever editor (IME) action is available on a view.
 */
public final class EditorAction implements ViewAction {

  @Override
  public Matcher<View> getConstraints() {
    return isDisplayed();
  }

  @Override
  public String getDescription() {
    return "input method editor";
  }

  @Override
  public void perform(UiController uiController, View view) {
    EditorInfo editorInfo = new EditorInfo();
    InputConnection inputConnection = view.onCreateInputConnection(editorInfo);
    if (inputConnection == null) {
      throw new PerformException.Builder()
        .withActionDescription(this.toString())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new IllegalStateException("View does not support input methods"))
        .build();
    }

    int actionId = editorInfo.actionId != 0 ? editorInfo.actionId :
      editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;

    if (actionId == EditorInfo.IME_ACTION_NONE) {
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new IllegalStateException("No available action on view"))
        .build();
    }

    if (!inputConnection.performEditorAction(actionId)) {
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new RuntimeException(String.format(
            "Failed to perform action %#x. Input connection no longer valid", actionId)))
        .build();
    }
  }
}
