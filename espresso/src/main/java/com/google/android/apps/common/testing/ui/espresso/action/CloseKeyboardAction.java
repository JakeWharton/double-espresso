package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.Matchers.anything;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.PerformException;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Closes soft keyboard.
 */
public final class CloseKeyboardAction implements ViewAction {

  private static final int NUM_RETRIES = 3;
  private static final String TAG = CloseKeyboardAction.class.getSimpleName();

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    return anything();
  }

  @Override
  public void perform(UiController uiController, View view) {
    // Retry in case of timeout exception to avoid flakiness in IMM.
    for (int i = 0; i < NUM_RETRIES; i++) {
      try {
        tryToCloseKeyboard(view, uiController);
        return;
      } catch (TimeoutException te) {
        Log.w(TAG, "Caught timeout exception. Retrying.");
        if (i == 2) {
          throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(te)
            .build();
        }
      }
    }
  }

  private void tryToCloseKeyboard(View view, UiController uiController) throws TimeoutException {
    InputMethodManager imm = (InputMethodManager) getRootActivity(uiController)
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    final AtomicInteger atomicResultCode = new AtomicInteger();
    final CountDownLatch latch = new CountDownLatch(1);

    ResultReceiver result = new ResultReceiver(null) {
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        atomicResultCode.set(resultCode);
        latch.countDown();
      }
    };

    if (!imm.hideSoftInputFromWindow(view.getWindowToken(), 0, result)) {
      Log.w(TAG, "Attempting to close soft keyboard, while it is not shown.");
      return;
    }

    try {
      if (!latch.await(2, TimeUnit.SECONDS)) {
        throw new TimeoutException("Wait on operation result timed out.");
      }
    } catch (InterruptedException e) {
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new RuntimeException("Waiting for soft keyboard close result was interrupted."))
        .build();
    }

    if (atomicResultCode.get() != InputMethodManager.RESULT_UNCHANGED_HIDDEN
        && atomicResultCode.get() != InputMethodManager.RESULT_HIDDEN) {
      String error =
          "Attempt to close the soft keyboard did not result in soft keyboard to be hidden."
          + "resultCode = " + atomicResultCode.get();
      Log.e(TAG, error);
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new RuntimeException(error))
        .build();
    }
  }

  private static Activity getRootActivity(UiController uiController) {
    Collection<Activity> resumedActivities =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      uiController.loopMainThreadUntilIdle();
      resumedActivities =
          ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    }
    Activity topActivity = getOnlyElement(resumedActivities);
    return topActivity;
  }

  @Override
  public String getDescription() {
    return "close keyboard";
  }
}
