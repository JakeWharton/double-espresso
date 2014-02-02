package com.google.android.apps.common.testing.testrunner;

/**
 * An enumeration of the lifecycle stages an activity undergoes.
 *
 * See the Activity javadoc for detailed documentation.
 */
public enum Stage {
  PRE_ON_CREATE, // Indicates that onCreate is being called before any onCreate code executes.
  CREATED, // Indicates that onCreate has been called.
  STARTED, // Indicates that onStart has been called.
  RESUMED, // Indicates that onResume has been called - activity is now visible to user.
  PAUSED, // Indicates that onPause has been called - activity is no longer in the foreground.
  STOPPED, // Indicates that onStop has been called - activity is no longer visible to the user
  RESTARTED, // Indicates that onResume has been called - we have navigated back to the activity
  DESTROYED // Indicates that onDestroy has been called - system is shutting down the activity
}
