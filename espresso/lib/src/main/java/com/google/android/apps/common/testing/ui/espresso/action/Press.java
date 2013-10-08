package com.google.android.apps.common.testing.ui.espresso.action;

/**
 * Returns different touch target sizes.
 */
public enum Press implements PrecisionDescriber {
  PINPOINT {
  @Override
    public float[] describePrecision() {
      float[] pinpoint = {1f, 1f};
      return pinpoint;
    }
  },
  FINGER {
    // average width of the index finger is 16 – 20 mm.
  @Override
    public float[] describePrecision() {
      float finger[] = {16f, 16f};
      return finger;
    }
  },
  // average width of an adult thumb is 25 mm (1 inch).
  THUMB {
  @Override
    public float[] describePrecision() {
      float thumb[] = {25f, 25f};
      return thumb;
    }
  };
}
