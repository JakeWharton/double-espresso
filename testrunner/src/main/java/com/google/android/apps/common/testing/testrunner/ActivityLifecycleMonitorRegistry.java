package com.google.android.apps.common.testing.testrunner;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An exposed registry instance to make it easy for callers to find the lifecycle monitor for their
 * application.
 *
 */
public final class ActivityLifecycleMonitorRegistry {

  private static final AtomicReference<ActivityLifecycleMonitor> lifecycleMonitor =
      new AtomicReference<ActivityLifecycleMonitor>(null);

  /**
   * Returns the ActivityLifecycleMonitor.
   *
   * This monitor is not guaranteed to be present under all instrumentations.
   *
   * @return ActivityLifecycleMonitor the monitor for this application.
   * @throws IllegalStateException if no monitor has been registered.
   */
  public static ActivityLifecycleMonitor getInstance() {
    ActivityLifecycleMonitor instance = lifecycleMonitor.get();
    if (null == instance) {
      throw new IllegalStateException("No lifecycle monitor registered! Are you running under an " +
        "Instrumentation which registers lifecycle monitors?");
    }
    return instance;
  }

  /**
   * Stores a lifecycle monitor in the registry.
   *
   * This is a global registry - so be aware of the impact of calling this method!
   * @param monitor the monitor for this application. Null deregisters any existing monitor.
   */
  public static void registerInstance(ActivityLifecycleMonitor monitor) {
    lifecycleMonitor.set(monitor);
  }

  private ActivityLifecycleMonitorRegistry() { }
}
