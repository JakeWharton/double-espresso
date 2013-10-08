package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitor;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.Root;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;
import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides the root View of the top-most Window, with which the user can interact. View is
 * guaranteed to be in a stable state - i.e. not pending any updates from the application.
 *
 * TODO(user): delegate the Window picking logic (which is built in right now) to a
 * Matcher<Window>.
 * This provider can only be accessed from the main thread.
 */
@Singleton
public final class RootViewPicker implements Provider<View> {

  private static final String TAG = RootViewPicker.class.getSimpleName();

  private final Provider<List<Root>> rootsOracle;
  private final UiController uiController;
  private final ActivityLifecycleMonitor activityLifecycleMonitor;

  @Inject
  RootViewPicker(Provider<List<Root>> rootsOracle, UiController uiController,
      ActivityLifecycleMonitor activityLifecycleMonitor) {
    this.rootsOracle = rootsOracle;
    this.uiController = uiController;
    this.activityLifecycleMonitor = activityLifecycleMonitor;
  }

  @Override
  public View get() {
    checkState(Looper.getMainLooper().equals(Looper.myLooper()), "must be called on main thread.");

    View rootView = findRootView();

    // we only want to propagate a root view that the user can interact with and is not
    // about to relay itself out. An app should be in this state the majority of the time,
    // if we happen not to be in this state at the moment, process the queue some more
    // we should come to it quickly enough.
    int loops = 0;

    while (rootView.isLayoutRequested() || !rootView.hasWindowFocus()) {
      if (loops < 3) {
        uiController.loopMainThreadUntilIdle();
      } else if (loops < 1001) {

        // loopUntil idle effectively is polling and pegs the cpu... if we dont have an update to
        // process immedately, we might have something coming very very soon.

        uiController.loopMainThreadForAtLeast(10);
      } else {
        // we've waited for the root view to be fully layed out and have window focus
        // for over 10 seconds. something is wrong.
        throw new RuntimeException("Waited for the root of the view hierarchy to have window " +
            "focus and not be requesting layout for over 10 seconds. Something is seriously " +
            "wrong. View: " + HumanReadables.describe(rootView));
      }

      rootView = findRootView();
      loops++;
    }

    return rootView;
  }

  private View findRootView() {
    Collection<Activity> resumedActivities =
        activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      uiController.loopMainThreadUntilIdle();
      resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    }
    if (resumedActivities.isEmpty()) {
      List<Activity> activities = Lists.newArrayList();
      for (Stage s : EnumSet.range(Stage.PRE_ON_CREATE, Stage.RESTARTED)) {
        activities.addAll(activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED));
      }
      if (activities.isEmpty()) {
        throw new RuntimeException("No activities found. Did you forget to launch the activity " +
            "by calling getActivity() or startActivitySync or similar?");
      }
      // well at least there are some activities in the pipeline - lets see if they resume.

      long [] waitTimes = {10, 50, 100, 500, TimeUnit.SECONDS.toMillis(2),
        TimeUnit.SECONDS.toMillis(30)};

      for (int waitIdx = 0; waitIdx < waitTimes.length; waitIdx++) {
        Log.w(TAG, "No activity currently resumed - waiting: " + waitTimes[waitIdx] +
            "ms for one to appear.");
        uiController.loopMainThreadForAtLeast(waitTimes[waitIdx]);
        resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
        if (!resumedActivities.isEmpty()) {
          break;
        }
      }
      throw new RuntimeException("No activities in stage RESUMED. Did you forget to launch the " +
          "activity. (test.getActivity() or similar)?");
    }
    Activity topActivity = getOnlyElement(resumedActivities);

    List<Root> roots = rootsOracle.get();
    if (roots.isEmpty() || roots.size() == 1) {
      // either reflection broke or things just are not that interesting.
      // Multiple roots only occur:
      //   when multiple activities are in some state of their lifecycle in the application
      //     - we don't care about this, since we only want to interact with the RESUMED
      //       activity, all other activities windows are not visible to the user so, out of
      //       scope.
      //   when a PopupWindow or PopupMenu is used
      //     - this is a case where we definitely want to consider the top most window, since
      //       it probably has the most useful info in it.
      //   when an android.app.dialog is shown
      //     - again, this is getting all the users attention, so it gets the test attention
      //       too.
      //
      // So generally - falling back to resumed activity's root view is often the right thing
      // to do.
      return topActivity.getWindow().getDecorView();
    }

    Log.i(TAG, "Roots: " + roots);

    IBinder resumedActivityToken = topActivity.getWindow().getDecorView()
        .getApplicationWindowToken();

    List<Root> activitySubpanels = Lists.newArrayList();
    for (Root root : roots) {
      if (root.getWindowLayoutParams().isPresent()) {
        int flags = root.getWindowLayoutParams().get().flags;
        boolean notFocusable = (flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 1;
        boolean notTouchable = (flags & WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) == 1;
        if (notFocusable || notTouchable) {
          // ignore windows the user cannot interact with - we can't interact with them either.
          // sure we could perform verifications on them - but we could never do anything else.
          // need to have a concrete use-case of how windows like these should be exposed to
          // tests.

          continue;
        }

        int type = root.getWindowLayoutParams().get().type;
        if (type != WindowManager.LayoutParams.TYPE_BASE_APPLICATION &&
            type < WindowManager.LayoutParams.LAST_APPLICATION_WINDOW) {
          IBinder windowToken = root.getDecorView().getWindowToken();
          IBinder appToken = root.getDecorView().getApplicationWindowToken();
          if (windowToken == appToken) {
            // windowtoken == appToken means this window isnt contained by any other windows.
            // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
            // therefore it must be a dialog box - let it be our root if its taking input.
            if (root.getDecorView().hasWindowFocus()) {
              return root.getDecorView();
            }
          }
        } else if (root.getDecorView().getApplicationWindowToken() == resumedActivityToken) {
          // this window is contained in our resumed activity's window.
          activitySubpanels.add(root);
        } // else - not in the resumed activity, not a dialog with focus - ignore.
      }
    }

    if (activitySubpanels.isEmpty()) {
      return topActivity.getWindow().getDecorView();
    } else {
      Root topSubpanel = activitySubpanels.get(0);
      for (Root subpanel : activitySubpanels) {
        if (subpanel.getWindowLayoutParams().get().type >
            topSubpanel.getWindowLayoutParams().get().type) {
          topSubpanel = subpanel;
        }
      }
      return topSubpanel.getDecorView();
    }
  }
}
