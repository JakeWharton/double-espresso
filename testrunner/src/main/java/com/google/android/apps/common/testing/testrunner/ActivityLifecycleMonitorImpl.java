package com.google.android.apps.common.testing.testrunner;

import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

final class ActivityLifecycleMonitorImpl implements ActivityLifecycleMonitor {
  private static final String TAG = "LifecycleMonitor";
  private final boolean declawThreadCheck;

  ActivityLifecycleMonitorImpl() {
    this(false);
  }

  //For Testing
  ActivityLifecycleMonitorImpl(boolean declawThreadCheck) {
    this.declawThreadCheck = declawThreadCheck;
  }



  // Accessed from any thread.
  private List<WeakReference<ActivityLifecycleCallback>> callbacks =
      new ArrayList<WeakReference<ActivityLifecycleCallback>>();

  // Only accessed on main thread.
  private List<ActivityStatus> activityStatuses = new ArrayList<ActivityStatus>();

  @Override
  public void addLifecycleCallback(ActivityLifecycleCallback callback) {
    // there will never be too many callbacks, so iterating over a list will probably
    // be faster then the constant time costs of setting up and maintaining a map.
    checkNotNull(callback);

    synchronized (callbacks) {
      boolean needsAdd = true;
      Iterator<WeakReference<ActivityLifecycleCallback>> refIter = callbacks.iterator();
      while (refIter.hasNext()) {
        ActivityLifecycleCallback storedCallback = refIter.next().get();
        if (null == storedCallback) {
          refIter.remove();
        } else if (storedCallback == callback) {
          needsAdd = false;
        }
      }
      if (needsAdd) {
        callbacks.add(new WeakReference<ActivityLifecycleCallback>(callback));
      }
    }
  }

  @Override
  public void removeLifecycleCallback(ActivityLifecycleCallback callback) {
    checkNotNull(callback);

    synchronized (callbacks) {
      Iterator<WeakReference<ActivityLifecycleCallback>> refIter = callbacks.iterator();
      while (refIter.hasNext()) {
        ActivityLifecycleCallback storedCallback = refIter.next().get();
        if (null == storedCallback) {
          refIter.remove();
        } else if (storedCallback == callback) {
          refIter.remove();
        }
      }
    }
  }

  @Override
  public Stage getLifecycleStageOf(Activity activity) {
    checkMainThread();
    checkNotNull(activity);
    Iterator<ActivityStatus> statusIterator = activityStatuses.iterator();
    while (statusIterator.hasNext()) {
      ActivityStatus status = statusIterator.next();
      Activity statusActivity = status.activityRef.get();
      if (null == statusActivity) {
        statusIterator.remove();
      } else if (activity == statusActivity) {
        return status.lifecycleStage;
      }
    }
    throw new IllegalArgumentException("Unknown activity: " + activity);
  }

  @Override
  public Collection<Activity> getActivitiesInStage(Stage stage) {
    checkMainThread();
    checkNotNull(stage);

    List<Activity> activities = new ArrayList<Activity>();
    Iterator<ActivityStatus> statusIterator = activityStatuses.iterator();
    while (statusIterator.hasNext()) {
      ActivityStatus status = statusIterator.next();
      Activity statusActivity = status.activityRef.get();
      if (null == statusActivity) {
        statusIterator.remove();
      } else if (stage == status.lifecycleStage) {
        activities.add(statusActivity);
      }
    }

    return activities;
  }

  void signalLifecycleChange(Stage stage, Activity activity) {
    // there are never too many activities in existance in an application - so we keep
    // track of everything in a single list.
    Log.d(TAG, "Lifecycle status change: " + activity + " in: " + stage);


    boolean needsAdd = true;
    Iterator<ActivityStatus> statusIterator = activityStatuses.iterator();
    while (statusIterator.hasNext()) {
      ActivityStatus status = statusIterator.next();
      Activity statusActivity = status.activityRef.get();
      if (null == statusActivity) {
        statusIterator.remove();
      } else if (activity == statusActivity) {
        needsAdd = false;
        status.lifecycleStage = stage;
      }
    }

    if (needsAdd) {
      activityStatuses.add(new ActivityStatus(activity, stage));
    }

    synchronized (callbacks) {
      Iterator<WeakReference<ActivityLifecycleCallback>> refIter = callbacks.iterator();
      while (refIter.hasNext()) {
        ActivityLifecycleCallback callback = refIter.next().get();
        if (null == callback) {
          refIter.remove();
        } else {
          try {
            Log.d(TAG, "running callback: " + callback);
            callback.onActivityLifecycleChanged(activity, stage);
            Log.d(TAG, "callback completes: " + callback);
          } catch (RuntimeException re) {
            Log.e(
                TAG,
                String.format(
                    "Callback threw exception! (callback: %s activity: %s stage: %s)",
                    callback,
                    activity,
                    stage),
                re);
          }
        }
      }
    }
  }

  private void checkMainThread() {
    if (declawThreadCheck) {
      return;
    }

    if (!Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
      throw new IllegalStateException("Querying activity state off main thread is not allowed.");
    }
  }

  private static class ActivityStatus {
    private final WeakReference<Activity> activityRef;
    private Stage lifecycleStage;

    ActivityStatus(Activity activity, Stage stage) {
      this.activityRef = new WeakReference<Activity>(checkNotNull(activity));
      this.lifecycleStage = checkNotNull(stage);
    }
  }
}
