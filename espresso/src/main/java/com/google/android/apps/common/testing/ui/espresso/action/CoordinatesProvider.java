package com.google.android.apps.common.testing.ui.espresso.action;

import android.view.View;

/**
 * Interface to implement calculation of Coordinates.
 */
public interface CoordinatesProvider {
  
  /**
   * Calculates coordinates of given view.
   * 
   * @param view the View which is used for the calculation. 
   * @return a float[] with x and y values of the calculated coordinates.  
   */
  public float[] calculateCoordinates(View view);
}
