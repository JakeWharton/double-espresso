package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

/**
 * Allows users fine grain control over idling policies.
 *
 * Espresso's default idling policies are suitable for most usecases - however
 * certain execution environments (like the ARM emulator) might be very slow.
 * This class allows users the ability to adjust defaults to sensible values
 * for their environments.
 */
public final class IdlingPolicies {

  private IdlingPolicies() { }

  private static volatile IdlingPolicy masterIdlingPolicy = new IdlingPolicy.Builder()
      .withIdlingTimeout(60)
      .withIdlingTimeoutUnit(TimeUnit.SECONDS)
      .throwAppNotIdleException()
      .build();


  private static volatile IdlingPolicy dynamicIdlingResourceErrorPolicy = new IdlingPolicy.Builder()
      .withIdlingTimeout(26)
      .withIdlingTimeoutUnit(TimeUnit.SECONDS)
      .throwIdlingResourceTimeoutException()
      .build();

  private static volatile IdlingPolicy dynamicIdlingResourceWarningPolicy =
      new IdlingPolicy.Builder()
        .withIdlingTimeout(5)
        .withIdlingTimeoutUnit(TimeUnit.SECONDS)
        .logWarning()
        .build();


  /**
   * Updates the IdlingPolicy used in UiController.loopUntil to detect AppNotIdleExceptions.
   *
   * @param timeout the timeout before an AppNotIdleException is created.
   * @param unit the unit of the timeout value.
   */
  public static void setMasterPolicyTimeout(long timeout, TimeUnit unit) {
    checkArgument(timeout > 0);
    checkNotNull(unit);
    masterIdlingPolicy = masterIdlingPolicy.toBuilder()
        .withIdlingTimeout(timeout)
        .withIdlingTimeoutUnit(unit)
        .build();
  }

  /**
   * Updates the IdlingPolicy used by IdlingResourceRegistry to determine when IdlingResources
   * timeout.
   *
   * @param timeout the timeout before an IdlingResourceTimeoutException is created.
   * @param unit the unit of the timeout value.
   */
  public static void setIdlingResourceTimeout(long timeout, TimeUnit unit) {
    checkArgument(timeout > 0);
    checkNotNull(unit);
    dynamicIdlingResourceErrorPolicy = dynamicIdlingResourceErrorPolicy.toBuilder()
        .withIdlingTimeout(timeout)
        .withIdlingTimeoutUnit(unit)
        .build();
  }


  public static IdlingPolicy getMasterIdlingPolicy() {
    return masterIdlingPolicy;
  }

  public static IdlingPolicy getDynamicIdlingResourceWarningPolicy() {
    return dynamicIdlingResourceWarningPolicy;
  }

  public static IdlingPolicy getDynamicIdlingResourceErrorPolicy() {
    return dynamicIdlingResourceErrorPolicy;
  }
}
