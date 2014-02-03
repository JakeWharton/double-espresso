package com.google.android.apps.common.testing.testrunner;

import android.app.Activity;

import java.util.Collection;

/**
 * Interface for tests to use when they need to query the activity lifecycle state.
 *
 * Activity lifecycle changes occur only on the UI thread - therefore listeners registered with
 * an ActivityLifecycleMonitor should expect to be invoked on the UI thread. The direct query
 * methods can only be called on the UI thread because otherwise they would not be able to return
 * consistent responses.
 * <p>
 * Retrieve instances of the monitor thru ActivityLifecycleMonitorRegistry.
 * </p>
 * <p>
 * Detecting these lifecycle states requires support from Instrumentation, therefore do not expect
 * an instance to be present under any arbitary instrumentation.
 * </p>
 */
public interface ActivityLifecycleMonitor {

  /**
   * Adds a new callback that will be notified when lifecycle changes occur.
   * <p>
   * Implementors will not hold a strong ref to the callback, the code which registers callbacks
   * is responsible for this. Code which registers callbacks should responsibly
   * remove their callback when it is no longer needed.
   * </p>
   * <p>
   * Callbacks are executed on the main thread of the application, and should take care not to
   * block or otherwise perform expensive operations as it will directly impact the application.
   * </p>
   *
   * @param callback an ActivityLifecycleCallback
   */
  void addLifecycleCallback(ActivityLifecycleCallback callback);

  /**
   * Removes a previously registered lifecycle callback.
   */
  void removeLifecycleCallback(ActivityLifecycleCallback callback);

  /**
   * Returns the current lifecycle stage of a given activity.
   *
   * This method can only return a consistant and correct answer
   * from the main thread, therefore callers should always invoke
   * it from the main thread and implementors are free to throw an
   * exception if the call is not made on the main thread.
   *
   * Implementors should ensure this method returns a consistant response if called from a
   * lifecycle callback also registered with this monitor (eg: it would be horriblely wrong if a
   * callback sees PAUSED and calls this method with the same activity and gets RESUMED.
   *
   * @param activity an activity in this application.
   * @return the lifecycle stage this activity is in.
   * @throws IllegalArgumentException if activity is unknown to the monitor.
   * @throws NullPointerException if activity is null.
   * @throws IllegalStateException if called off the main thread.
   */
  Stage getLifecycleStageOf(Activity activity);

  /**
   * Returns all activities in a given stage of their lifecycle.
   * <p>
   * This method can only return a consistant and correct answer from the main thread, therefore
   * callers should always invoke it from the main thread and implementors are free to throw an
   * exception if the call is not made on the main thread.
   * </p>
   * <p>
   * Implementors should ensure this method returns a consistant response if called from a
   * lifecycle callback also registered with this monitor (eg: it would be horriblely wrong if a
   * callback sees PAUSED and calls this method with the PAUSED and does not see its activity in
   * the response.
   * </p>
   *
   * <p>
   * Callers should be aware that the monitor implementation may not hold strong references to the
   * Activities in the application. Therefore stages which are considered end stages or eligible for
   * garbage collection on low memory situations may not return an instance of a particular activity
   * if it has been garbage collected.
   *
   * @param stage the stage to query for.
   * @return a snapshot Collection of activities in the given stage. This collection may be empty.
   * @throws IllegalStateException if called from outside the main thread.
   */
  Collection<Activity> getActivitiesInStage(Stage stage);
}
