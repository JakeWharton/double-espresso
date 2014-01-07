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
 * Executes different click types to given position.
 */
public enum Tap implements Tapper {
  SINGLE {
  @Override
    public Tapper.Status sendTap(UiController uiController, float[] coordinates,
        float[] precision) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);
      DownResultHolder res = sendDown(uiController, coordinates, precision);
      try {
        if (!sendUp(uiController, res.down)) {
          Log.d(TAG, "Injection of up event as part of the click failed. Send cancel event.");
          sendCancel(uiController, res.down);
          return Tapper.Status.FAILURE;
        }
      } finally {
        res.down.recycle();
        res.down = null;
      }
      return res.longPress ? Tapper.Status.WARNING : Tapper.Status.SUCCESS;
    }
  },
  LONG {
  @Override
    public Tapper.Status sendTap(UiController uiController, float[] coordinates,
        float[] precision) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);

      MotionEvent downEvent = sendDown(uiController, coordinates, precision).down;
      try {
        // Duration before a press turns into a long press.
        // Factor 1.5 is needed, otherwise a long press is not safely detected.
        // See android.test.TouchUtils longClickView
        long longPressTimeout = (long) (ViewConfiguration.getLongPressTimeout() * 1.5f);
        uiController.loopMainThreadForAtLeast(longPressTimeout);

        if (!sendUp(uiController, downEvent)) {
          sendCancel(uiController, downEvent);
          return Tapper.Status.FAILURE;
        }
      } finally {
        downEvent.recycle();
        downEvent = null;
      }
      return Tapper.Status.SUCCESS;
    }
  },
  DOUBLE {
  @Override
    public Tapper.Status sendTap(UiController uiController, float[] coordinates,
        float[] precision) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);
      Tapper.Status stat = SINGLE.sendTap(uiController, coordinates, precision);
      if (stat == Tapper.Status.FAILURE) {
        return Tapper.Status.FAILURE;
      }

      Tapper.Status secondStat = SINGLE.sendTap(uiController, coordinates, precision);

      if (secondStat == Tapper.Status.FAILURE) {
        return Tapper.Status.FAILURE;
      }

      if (secondStat == Tapper.Status.WARNING || stat == Tapper.Status.WARNING) {
        return Tapper.Status.WARNING;
      } else {
        return Tapper.Status.SUCCESS;
      }
    }
  };

  private static final String TAG = Tap.class.getSimpleName();
  @VisibleForTesting
  static final int MAX_CLICK_ATTEMPTS = 3;

  private static DownResultHolder sendDown(
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
        DownResultHolder res = new DownResultHolder();
        res.down = motionEvent;
        res.longPress = longPress;
        return res;
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

  private static boolean sendUp(UiController uiController, MotionEvent downEvent) {
    checkNotNull(uiController);
    checkNotNull(downEvent);

    MotionEvent motionEvent = null;
    try {
      // Up press.
      motionEvent = MotionEvent.obtain(downEvent.getDownTime(),
          SystemClock.uptimeMillis(),
          MotionEvent.ACTION_UP,
          downEvent.getX(),
          downEvent.getY(),
          0);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        Log.d(TAG, String.format(
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

  private static void sendCancel(UiController uiController, MotionEvent downEvent) {
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

  private static class DownResultHolder {
    private MotionEvent down;
    private boolean longPress;
  }

}
