package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Joiner;

import android.os.Looper;

import java.util.List;

/**
 * An exception which indicates that the App has not become idle even after the specified duration.
 */
public final class AppNotIdleException extends RuntimeException implements EspressoException {

  private AppNotIdleException(String description) {
    super(description);
  }

  /**
   * Creates a new AppNotIdleException suitable for erroring out a test case.
   *
   * This should be called only from the main thread if the app does not idle out within the
   * specified duration.
   *
   * @param idleConditions list of idleConditions that failed to become idle.
   * @param loopCount number of times it was tried to check if they became idle.
   * @param seconds number of seconds that was tried before giving up.
   *
   * @return a AppNotIdleException suitable to be thrown on the instrumentation thread.
   */
  @Deprecated
  public static AppNotIdleException create(List<String> idleConditions, int loopCount,
      int seconds) {
    checkState(Looper.myLooper() == Looper.getMainLooper());
    String errorMessage = String.format("App not idle within timeout of %s seconds even" +
        "after trying for %s iterations. The following Idle Conditions failed %s",
        seconds, loopCount, Joiner.on(",").join(idleConditions));
    return new AppNotIdleException(errorMessage);
  }

  /**
   * Creates a new AppNotIdleException suitable for erroring out a test case.
   *
   * This should be called only from the main thread if the app does not idle out within the
   * specified duration.
   *
   * @param idleConditions list of idleConditions that failed to become idle.
   * @param message a message about the failure.
   *
   * @return a AppNotIdleException suitable to be thrown on the instrumentation thread.
   */
  static AppNotIdleException create(List<String> idleConditions, String message) {
    String errorMessage = String.format("%s The following Idle Conditions failed %s.",
        message, Joiner.on(",").join(idleConditions));
    return new AppNotIdleException(errorMessage);
  }
}

