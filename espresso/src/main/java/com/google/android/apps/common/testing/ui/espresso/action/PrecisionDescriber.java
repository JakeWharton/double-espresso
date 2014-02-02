package com.google.android.apps.common.testing.ui.espresso.action;

/**
 * Interface to implement size of click area.
 */
public interface PrecisionDescriber {

  /**
   * Different touch target sizes.
   *
   * @return a float[] with x and y values of size of click area.
   */
  public float[] describePrecision();
}
