package com.google.android.apps.common.testing.ui.espresso.matcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitor;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.NoActivityResumedException;
import com.google.android.apps.common.testing.ui.espresso.Root;
import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.List;

/**
 * A collection of matchers for {@link Root} objects.
 */
public final class RootMatchers {

  private RootMatchers() {}

  /**
   * Espresso's default {@link Root} matcher.
   */
  @SuppressWarnings("unchecked")
  public static final Matcher<Root> DEFAULT =
      allOf(
        hasWindowLayoutParams(),
        allOf(
             anyOf(
                  allOf(isDialog(), withDecorView(hasWindowFocus())),
                  isSubwindowOfCurrentActivity()),
             isFocusable()));


  /**
   * Matches {@link Root}s that can take window focus.
   */
  public static Matcher<Root> isFocusable() {
    return new TypeSafeMatcher<Root>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("is focusable");
      }

      @Override
      public boolean matchesSafely(Root root) {
        int flags = root.getWindowLayoutParams().get().flags;
        boolean r = !((flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0);
        return r;
      }
    };
  }

  /**
   * Matches {@link Root}s that can receive touch events.
   */
  public static Matcher<Root> isTouchable() {
    return new TypeSafeMatcher<Root>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("is touchable");
      }

      @Override
      public boolean matchesSafely(Root root) {
        int flags = root.getWindowLayoutParams().get().flags;
        boolean r = !((flags & WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != 0);
        return r;
      }
    };
  }

  /**
   * Matches {@link Root}s that are dialogs (i.e. is not a window of the currently resumed
   * activity).
   */
  public static Matcher<Root> isDialog() {
    return new TypeSafeMatcher<Root>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("is dialog");
      }

      @Override
      public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if ((type != WindowManager.LayoutParams.TYPE_BASE_APPLICATION
            && type < WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)) {
          IBinder windowToken = root.getDecorView().getWindowToken();
          IBinder appToken = root.getDecorView().getApplicationWindowToken();
          if (windowToken == appToken) {
            // windowToken == appToken means this window isn't contained by any other windows.
            // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
            // therefore it must be a dialog box.
            return true;
          }
        }
        return false;
      }
    };
  }

  /**
   * Matches {@link Root}s with decor views that match the given view matcher.
   */
  public static Matcher<Root> withDecorView(final Matcher<View> decorViewMatcher) {
    checkNotNull(decorViewMatcher);
    return new TypeSafeMatcher<Root>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("with decor view ");
        decorViewMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(Root root) {
        return decorViewMatcher.matches(root.getDecorView());
      }
    };
  }

  private static Matcher<View> hasWindowFocus() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has window focus");
      }

      @Override
      public boolean matchesSafely(View view) {
        return view.hasWindowFocus();
      }
    };
  }

  private static Matcher<Root> hasWindowLayoutParams() {
    return new TypeSafeMatcher<Root>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("has window layout params");
      }

      @Override
      public boolean matchesSafely(Root root) {
        if (!root.getWindowLayoutParams().isPresent()) {
          return false;
        }
        return true;
      }
    };
  }

  private static Matcher<Root> isSubwindowOfCurrentActivity() {
    return new TypeSafeMatcher<Root>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("is subwindow of current activity");
      }

      @Override
      public boolean matchesSafely(Root root) {
        boolean r =
            getResumedActivityTokens().contains(root.getDecorView().getApplicationWindowToken());
        return r;
      }
    };
  }

  private static List<IBinder> getResumedActivityTokens() {
    ActivityLifecycleMonitor activityLifecycleMonitor =
        ActivityLifecycleMonitorRegistry.getInstance();
    Collection<Activity> resumedActivities =
        activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      throw new NoActivityResumedException("At least one activity should be in RESUMED stage.");
    }
    List<IBinder> tokens = Lists.newArrayList();
    for (Activity activity : resumedActivities) {
      tokens.add(activity.getWindow().getDecorView().getApplicationWindowToken());
    }
    return tokens;
  }
}
