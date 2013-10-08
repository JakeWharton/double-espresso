package com.google.android.apps.common.testing.testrunner;

import android.app.Activity;

/**
 * Callback for monitoring activity lifecycle events. These callbacks are invoked on the main
 * thread, so any long operations or violating the strict mode policies should be avoided.
 *
 */
public interface ActivityLifecycleCallback {

  /**
   * Called on the main thread after an activity has processed its lifecycle change event
   * (for example onResume or onStart)
   *
   * @param activity The activity
   * @param stage its current stage.
   */
  public void onActivityLifecycleChanged(Activity activity, Stage stage);
}
