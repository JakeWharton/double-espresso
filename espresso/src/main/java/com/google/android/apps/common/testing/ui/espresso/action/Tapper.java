package com.google.android.apps.common.testing.ui.espresso.action;

import com.google.android.apps.common.testing.ui.espresso.UiController;

/**
 * Interface to implement different click types.
 */
public interface Tapper {

  /**
   * The result of the tap.
   */
  public enum Status {
    /**
     * The Tap action completed successfully.
     */
    SUCCESS,
    /**
     * The action seemed to have completed - but may have been misinterpreted
     * by the application. (For Example a TAP became a LONG PRESS by measuring
     * its time between the down and up events).
     */
    WARNING,
    /**
     * Injecting the event was a complete failure.
     */
    FAILURE }

  /**
   * Sends a MotionEvent to the given UiController.
   *
   * @param uiController a UiController to use to send MotionEvents to the screen.
   * @param coordinates a float[] with x and y values of center of the tap.
   * @param precision  a float[] with x and y values of precision of the tap.
   * @return The status of the tap.
   */
  public Status sendTap(UiController uiController, float[] coordinates, float[] precision);
}
