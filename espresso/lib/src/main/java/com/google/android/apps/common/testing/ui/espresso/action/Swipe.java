package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.UiController;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Executes different swipe types to given positions.
 */
public enum Swipe implements Swiper {

  /** Swipes quickly between the co-ordinates. */
  FAST {
  @Override
    public Swiper.Status sendSwipe(UiController uiController, float[] startCoordinates,
        float[] endCoordinates, float[] precision) {
      return sendLinearSwipe(uiController, startCoordinates, endCoordinates, precision,
          SWIPE_FAST_DURATION_MS);
    }
  },

  /** Swipes deliberately slowly between the co-ordinates, to aid in visual debugging. */
  SLOW {
  @Override
    public Swiper.Status sendSwipe(UiController uiController, float[] startCoordinates,
        float[] endCoordinates, float[] precision) {
      return sendLinearSwipe(uiController, startCoordinates, endCoordinates, precision,
          SWIPE_SLOW_DURATION_MS);
    }
  };

  private static final String TAG = Swipe.class.getSimpleName();

  /** The number of motion events to send for each swipe. */
  private static final int SWIPE_EVENT_COUNT = 10;

  /** Length of time a "fast" swipe should last for, in milliseconds. */
  private static final int SWIPE_FAST_DURATION_MS = 100;

  /** Length of time a "slow" swipe should last for, in milliseconds. */
  private static final int SWIPE_SLOW_DURATION_MS = 1500;

  private static float[][] interpolate(float[] start, float[] end, int steps) {
    checkElementIndex(1, start.length);
    checkElementIndex(1, end.length);

    float[][] res = new float[steps][2];

    for (int i = 1; i < steps + 1; i++) {
      res[i - 1][0] = start[0] + (end[0] - start[0]) * i / (steps + 2f);
      res[i - 1][1] = start[1] + (end[1] - start[1]) * i / (steps + 2f);
    }

    return res;
  }

  private static Swiper.Status sendLinearSwipe(UiController uiController, float[] startCoordinates,
      float[] endCoordinates, float[] precision, int duration) {
    checkNotNull(uiController);
    checkNotNull(startCoordinates);
    checkNotNull(endCoordinates);
    checkNotNull(precision);

    float[][] steps = interpolate(startCoordinates, endCoordinates, SWIPE_EVENT_COUNT);
    final int delayBetweenMovements = duration / steps.length;

    MotionEvent downEvent = MotionEvents.sendDown(uiController, steps[0], precision).down;
    try {
      for (int i = 1; i < steps.length; i++) {
        if (!MotionEvents.sendMovement(uiController, downEvent, steps[i])) {
          Log.e(TAG, "Injection of move event as part of the swipe failed. Sending cancel event.");
          MotionEvents.sendCancel(uiController, downEvent);
          return Swiper.Status.FAILURE;
        }

        long desiredTime = downEvent.getDownTime() + delayBetweenMovements * i;
        long timeUntilDesired = desiredTime - SystemClock.uptimeMillis();
        if (timeUntilDesired > 10) {
          uiController.loopMainThreadForAtLeast(timeUntilDesired);
        }
      }

      if (!MotionEvents.sendUp(uiController, downEvent, endCoordinates)) {
        Log.e(TAG, "Injection of up event as part of the swipe failed. Sending cancel event.");
        MotionEvents.sendCancel(uiController, downEvent);
        return Swiper.Status.FAILURE;
      }
    } finally {
      downEvent.recycle();
    }
    return Swiper.Status.SUCCESS;
  }

}
