package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.ViewAction;

import android.view.KeyEvent;

/**
 * A collection of common {@link ViewActions}.
 */
public final class ViewActions {

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
    return new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER);
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
    checkNotNull(rollbackAction);
    return new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER,
        rollbackAction);
  }

  /**
   * Returns an action that performs a swipe right-to-left across the vertical center of the
   * view.<br>
   * <br>
   * View constraints:
   * <ul>
   * <li>must be displayed on screen
   * <ul>
   */
  public static ViewAction swipeLeft() {
    return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_RIGHT,
        GeneralLocation.CENTER_LEFT, Press.FINGER);
  }

  /**
   * Returns an action that performs a swipe left-to-right across the vertical center of the
   * view.<br>
   * <br>
   * View constraints:
   * <ul>
   * <li>must be displayed on screen
   * <ul>
   */
  public static ViewAction swipeRight() {
    return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT,
        GeneralLocation.CENTER_RIGHT, Press.FINGER);
  }

  /**
   * Returns an action that closes soft keyboard. If the keyboard is already closed, it is a no-op.
   */
  public static ViewAction closeSoftKeyboard() {
    return new CloseKeyboardAction();
  }

  /**
   * Returns an action that presses the current action button (next, done, search, etc) on the IME
   * (Input Method Editor). The selected view will have its onEditorAction method called.
   */
  public static ViewAction pressImeActionButton() {
    return new EditorAction();
  }

  /**
   * Returns an action that clicks the back button.
   */
  public static ViewAction pressBack() {
    return pressKey(KeyEvent.KEYCODE_BACK);
  }

  /**
   * Returns an action that presses the hardware menu key.
   */
  public static ViewAction pressMenuKey() {
    return pressKey(KeyEvent.KEYCODE_MENU);
  }

  /**
   * Returns an action that presses the key specified by the keyCode (eg. Keyevent.KEYCODE_BACK).
   */
  public static ViewAction pressKey(int keyCode) {
    return new KeyEventAction(new EspressoKey.Builder().withKeyCode(keyCode).build());
  }

  /**
   * Returns an action that presses the specified key with the specified modifiers.
   */
  public static ViewAction pressKey(EspressoKey key) {
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
    return new GeneralClickAction(Tap.DOUBLE, GeneralLocation.CENTER, Press.FINGER);
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
    return new GeneralClickAction(Tap.LONG, GeneralLocation.CENTER, Press.FINGER);
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
    return new ScrollToAction();
  }

  /**
   * Returns an action that types the provided string into the view.
   * Appending a \n to the end of the string translates to a ENTER key event. Note: this method
   * does not change cursor position in the focused view - text is inserted at the location where
   * the cursor is currently pointed.<br>
   * <br>
   * View preconditions:
   * <ul>
   * <li>must be displayed on screen
   * <li>must support input methods
   * <li>must be already focused
   * <ul>
   */
  public static ViewAction typeTextIntoFocusedView(String stringToBeTyped) {
    return new TypeTextAction(stringToBeTyped, false /* tapToFocus */);
  }

  /**
   * Returns an action that selects the view (by clicking on it) and types the provided string into
   * the view. Appending a \n to the end of the string translates to a ENTER key event. Note: this
   * method performs a tap on the view before typing to force the view into focus, if the view 
   * already contains text this tap may place the cursor at an arbitrary position within the text.
   * <br>
   * <br>
   * View preconditions:
   * <ul>
   * <li>must be displayed on screen
   * <li>must support input methods
   * <ul>
   */
  public static ViewAction typeText(String stringToBeTyped) {
    return new TypeTextAction(stringToBeTyped);
  }
}
