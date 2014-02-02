package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.testrunner.UsageTrackerRegistry;
import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;
import com.google.android.apps.common.testing.ui.espresso.PerformException;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.common.annotations.VisibleForTesting;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Facilitates sending of motion events to a {@link UiController}.
 */
final class MotionEvents {

  private static final String TAG = MotionEvents.class.getSimpleName();

  @VisibleForTesting
  static final int MAX_CLICK_ATTEMPTS = 3;

  private MotionEvents() {
    // Shouldn't be instantiated
  }

  static DownResultHolder sendDown(
      UiController uiController, float[] coordinates, float[] precision) {
    checkNotNull(uiController);
    checkNotNull(coordinates);
    checkNotNull(precision);

    for (int retry = 0; retry < MAX_CLICK_ATTEMPTS; retry++) {
      MotionEvent motionEvent = null;
      try {
        // Algorithm of sending click event adopted from android.test.TouchUtils.
        // When the click event was first initiated. Needs to be same for both down and up press
        // events.
        long downTime = SystemClock.uptimeMillis();

        // Down press.
        motionEvent = MotionEvent.obtain(downTime,
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_DOWN,
            coordinates[0],
            coordinates[1],
            0, // pressure
            1, // size
            0, // metaState
            precision[0], // xPrecision
            precision[1], // yPrecision
            0,  // deviceId
            0); // edgeFlags
        // The down event should be considered a tap if it is long enough to be detected
        // but short enough not to be a long-press. Assume that TapTimeout is set at least
        // twice the detection time for a tap (no need to sleep for the whole TapTimeout since
        // we aren't concerned about scrolling here).
        long isTapAt = downTime + (ViewConfiguration.getTapTimeout() / 2);

        boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

        while (true) {
          long delayToBeTap = isTapAt - SystemClock.uptimeMillis();
          if (delayToBeTap <= 10) {
            break;
          }
          // Sleep only a fraction of the time, since there may be other events in the UI queue
          // that could cause us to start sleeping late, and then oversleep.
          uiController.loopMainThreadForAtLeast(delayToBeTap / 4);
        }

        boolean longPress = false;
        if (SystemClock.uptimeMillis() > (downTime + ViewConfiguration.getLongPressTimeout())) {
          longPress = true;
          Log.e(TAG, "Overslept and turned a tap into a long press");
          UsageTrackerRegistry.getInstance().trackUsage("Espresso.Tap.Error.tapToLongPress");
        }

        if (!injectEventSucceeded) {
          motionEvent.recycle();
          motionEvent = null;
          continue;
        }

        return new DownResultHolder(motionEvent, longPress);
      } catch (InjectEventSecurityException e) {
        throw new PerformException.Builder()
          .withActionDescription("Send down montion event")
          .withViewDescription("unknown") // likely to be replaced by FailureHandler
          .withCause(e)
          .build();
      }
    }
    throw new PerformException.Builder()
      .withActionDescription(String.format("click (after %s attempts)", MAX_CLICK_ATTEMPTS))
      .withViewDescription("unknown") // likely to be replaced by FailureHandler
      .build();
  }

  static boolean sendUp(UiController uiController, MotionEvent downEvent) {
    return sendUp(uiController, downEvent, new float[] { downEvent.getX(), downEvent.getY() });
  }

  static boolean sendUp(UiController uiController, MotionEvent downEvent, float[] coordinates) {
    checkNotNull(uiController);
    checkNotNull(downEvent);
    checkNotNull(coordinates);

    MotionEvent motionEvent = null;
    try {
      // Up press.
      motionEvent = MotionEvent.obtain(downEvent.getDownTime(),
          SystemClock.uptimeMillis(),
          MotionEvent.ACTION_UP,
          coordinates[0],
          coordinates[1],
          0);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        Log.e(TAG, String.format(
            "Injection of up event failed (corresponding down event: %s)", downEvent.toString()));
        return false;
      }
    } catch (InjectEventSecurityException e) {
      throw new PerformException.Builder()
        .withActionDescription(
            String.format("inject up event (corresponding down event: %s)", downEvent.toString()))
        .withViewDescription("unknown") // likely to be replaced by FailureHandler
        .withCause(e)
        .build();
    } finally {
      if (null != motionEvent) {
        motionEvent.recycle();
        motionEvent = null;
      }
    }
    return true;
  }

  static void sendCancel(UiController uiController, MotionEvent downEvent) {
    checkNotNull(uiController);
    checkNotNull(downEvent);

    MotionEvent motionEvent = null;
    try {
      // Up press.
      motionEvent = MotionEvent.obtain(downEvent.getDownTime(),
          SystemClock.uptimeMillis(),
          MotionEvent.ACTION_CANCEL,
          downEvent.getX(),
          downEvent.getY(),
          0);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        throw new PerformException.Builder()
          .withActionDescription(String.format(
            "inject cancel event (corresponding down event: %s)", downEvent.toString()))
          .withViewDescription("unknown") // likely to be replaced by FailureHandler
          .build();
      }
    } catch (InjectEventSecurityException e) {
      throw new PerformException.Builder()
        .withActionDescription(String.format(
          "inject cancel event (corresponding down event: %s)", downEvent.toString()))
        .withViewDescription("unknown") // likely to be replaced by FailureHandler
        .withCause(e)
        .build();
    } finally {
      if (null != motionEvent) {
        motionEvent.recycle();
        motionEvent = null;
      }
    }
  }

  static boolean sendMovement(UiController uiController, MotionEvent downEvent,
      float[] coordinates) {
    checkNotNull(uiController);
    checkNotNull(downEvent);
    checkNotNull(coordinates);

    MotionEvent motionEvent = null;
    try {
      motionEvent = MotionEvent.obtain(downEvent.getDownTime(),
          SystemClock.uptimeMillis(),
          MotionEvent.ACTION_MOVE,
          coordinates[0],
          coordinates[1],
          0);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        Log.e(TAG, String.format(
            "Injection of motion event failed (corresponding down event: %s)",
            downEvent.toString()));
        return false;
      }
    } catch (InjectEventSecurityException e) {
      throw new PerformException.Builder()
        .withActionDescription(String.format(
          "inject motion event (corresponding down event: %s)", downEvent.toString()))
        .withViewDescription("unknown") // likely to be replaced by FailureHandler
        .withCause(e)
        .build();
    } finally {
      if (null != motionEvent) {
        motionEvent.recycle();
        motionEvent = null;
      }
    }

    return true;
  }

  /**
   * Holds the result of a down motion.
   */
  static class DownResultHolder {
    public final MotionEvent down;
    public final boolean longPress;

    DownResultHolder(MotionEvent down, boolean longPress) {
      this.down = down;
      this.longPress = longPress;
    }
  }

}
