package com.google.android.apps.common.testing.ui.espresso;

/**
 * Represents a resource of an application under test which can cause asynchronous background work
 * to happen during test execution (e.g. an intent service that processes a button click). By
 * default, {@link Espresso} synchronizes all view operations with the UI thread as well as
 * AsyncTasks; however, it has no way of doing so with "hand-made" resources. In such cases, test
 * authors can register the custom resource and {@link Espresso} will wait for the resource to
 * become idle prior to executing a view operation.
 * <br><br>
 * <b>Important Note:</b> it is assumed that the resource stays idle most of the time.
 */
public interface IdlingResource {

  /**
   * Returns the name of the resources (used for logging and idempotency  of registration).
   */
  public String getName();

  /**
   * Returns {@code true} if resource is currently idle. Espresso will <b>always</b> call this
   * method from the main thread, therefore it should be non-blocking and return immediately.
   */
  public boolean isIdleNow();

  /**
   * Registers the given {@link ResourceCallback} with the resource. Espresso will call this method:
   * <ul>
   * <li>with its implementation of {@link ResourceCallback} so it can be notified asynchronously
   * that your resource is idle
   * <li>from the main thread, but you are free to execute the callback's onTransitionToIdle from
   * any thread
   * <li>once (when it is initially given a reference to your IdlingResource)
   * </ul>
   * <br>
   * You only need to call this upon transition from busy to idle - if the resource is already idle
   * when the method is called invoking the call back is optional and has no significant impact.
   */
  public void registerIdleTransitionCallback(ResourceCallback callback);

  /**
   * Registered by an {@link IdlingResource} to notify Espresso of a transition to idle.
   */
  public interface ResourceCallback {
    /**
     * Called when the resource goes from busy to idle.
     */
    public void onTransitionToIdle();
  }
}
