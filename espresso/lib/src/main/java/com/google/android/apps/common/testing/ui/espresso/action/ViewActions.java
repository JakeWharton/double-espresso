package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.testrunner.UsageTracker;
import com.google.android.apps.common.testing.testrunner.UsageTrackerRegistry;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;

import android.view.KeyEvent;

/**
 * A collection of common {@link ViewActions}.
 */
public final class ViewActions {

  private static final UsageTracker usageTracker;

  static {
    usageTracker = UsageTrackerRegistry.getInstance();
  }

  private ViewActions() {}

  /**
   * Returns an action that clears text on the view.<br>
   * <br>
   * View constraints:
   * <ul>
   * <li>must be displayed on screen
   * <ul>
   */
  public static ViewAction clearText() {
    usageTracker.trackUsage("Espresso.ViewActions.clearText");
    return new ClearTextAction();
  }

  /**
   * Returns an action that clicks the view.<br>
   * <br>
   * View constraints:
   * <ul>
   * <li>must be displayed on screen
   * <ul>
   */
  public static ViewAction click() {
    usageTracker.trackUsage("Espresso.ViewActions.click");
    return new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.PINPOINT);
  }

  /**
   * Returns an action that performs a single click on the view.
   *
   * If the click takes longer than the 'long press' duration (which is possible) the provided
   * rollback action is invoked on the view and a click is attempted again.
   *
   * This is only necessary if the view being clicked on has some different behaviour for long press
   * versus a normal tap.
   *
   * For example - if a long press on a particular view element opens a popup menu -
   * ViewActions.pressBack() may be an acceptable rollback action.
   *
   * <br>
   * View constraints:
   * <ul>
   * <li>must be displayed on screen</li>
   * <li>any constraints of the rollbackAction</li>
   * <ul>
   */
  public static ViewAction click(ViewAction rollbackAction) {
    usageTracker.trackUsage("Espresso.ViewActions.click(ViewAction)");
    checkNotNull(rollbackAction);
    return new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.PINPOINT,
        rollbackAction);
  }

  /**
   * Returns an action that closes soft keyboard. If the keyboard is already closed, it is a no-op.
   */
  public static ViewAction closeSoftKeyboard() {
    usageTracker.trackUsage("Espresso.ViewActions.closeSoftKeyboard");
    return new CloseKeyboardAction();
  }

  /**
   * Returns an action that presses the current action button (next, done, search, etc) on the IME
   * (Input Method Editor). The selected view will have its onEditorAction method called.
   */
  public static ViewAction pressImeActionButton() {
    usageTracker.trackUsage("Espresso.ViewActions.pressImeActionButton");
    return new EditorAction();
  }

  /**
   * Returns an action that clicks the back button.
   */
  public static ViewAction pressBack() {
    usageTracker.trackUsage("Espresso.ViewActions.pressBack");
    return pressKey(KeyEvent.KEYCODE_BACK);
  }

  /**
   * Returns an action that presses the hardware menu key.
   */
  public static ViewAction pressMenuKey() {
    usageTracker.trackUsage("Espresso.ViewActions.pressMenuKey");
    return pressKey(KeyEvent.KEYCODE_MENU);
  }

  /**
   * Returns an action that presses the key specified by the keyCode (eg. Keyevent.KEYCODE_BACK).
   */
  public static ViewAction pressKey(int keyCode) {
    usageTracker.trackUsage("Espresso.ViewActions.pressKey(int)");
    return new KeyEventAction(new EspressoKey.Builder().withKeyCode(keyCode).build());
  }

  /**
   * Returns an action that presses the specified key with the specified modifiers.
   */
  public static ViewAction pressKey(EspressoKey key) {
    usageTracker.trackUsage("Espresso.ViewActions.pressKey(EspressoKey)");
    return new KeyEventAction(key);
  }

  /**
   * Returns an action that double clicks the view.<br>
   * <br>
   * View preconditions:
   * <ul>
   * <li>must be displayed on screen
   * <ul>
   */
  public static ViewAction doubleClick() {
    usageTracker.trackUsage("Espresso.ViewActions.doubleClick");
    return new GeneralClickAction(Tap.DOUBLE, GeneralLocation.CENTER, Press.PINPOINT);
  }

  /**
   * Returns an action that long clicks the view.<br>
   *
   * <br>
   * View preconditions:
   * <ul>
   * <li>must be displayed on screen
   * <ul>
   */
  public static ViewAction longClick() {
    usageTracker.trackUsage("Espresso.ViewActions.longClick");
    return new GeneralClickAction(Tap.LONG, GeneralLocation.CENTER, Press.PINPOINT);
  }

  /**
   * Returns an action that scrolls to the view.<br>
   * <br>
   * View preconditions:
   * <ul>
   * <li>must be a descendant of ScrollView
   * <li>must have visibility set to View.VISIBLE
   * <ul>
   */
  public static ViewAction scrollTo() {
    usageTracker.trackUsage("Espresso.ViewActions.scrollTo");
    return new ScrollToAction();
  }

  /**
   * Returns an action that selects the view (by clicking on it) and types the provided string into
   * the view. Appending a \n to the end of the string translates to a ENTER key event. <br>
   * <br>
   * View preconditions:
   * <ul>
   * <li>must be displayed on screen
   * <li>must support input methods
   * <ul>
   */
  public static ViewAction typeText(String stringToBeTyped) {
    usageTracker.trackUsage("Espresso.ViewActions.typeText");
    return new TypeTextAction(stringToBeTyped);
  }
}
