package com.google.android.apps.common.testing.ui.espresso;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressMenuKey;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isRoot;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withClassName;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;

import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.google.android.apps.common.testing.ui.espresso.base.BaseLayerModule;
import com.google.android.apps.common.testing.ui.espresso.base.IdlingResourceRegistry;
import com.google.android.apps.common.testing.ui.espresso.util.TreeIterables;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.view.ViewConfiguration;

import dagger.ObjectGraph;

import org.hamcrest.Matcher;

/**
 * Entry point to the Espresso framework. Test authors can initiate testing by using one of the on*
 * methods (e.g. onView) or perform top-level user actions (e.g. pressBack).
 */
public final class Espresso {

  static ObjectGraph espressoGraph() {
    return GraphHolder.graph();
  }

  private Espresso() {}

  /**
   * Creates an {@link PartiallyScopedViewInteraction} for a given view. Note: the view has
   * to be part of the  view hierarchy. This may not be the case if it is rendered as part of
   * an AdapterView (e.g. ListView). If this is the case, use Espresso.onData to load the view
   * first.
   *
   * @param viewMatcher used to select the view.
   * @see #onData
   */
  public static ViewInteraction onView(final Matcher<View> viewMatcher) {
    return espressoGraph().plus(new ViewInteractionModule(viewMatcher)).get(ViewInteraction.class);
  }



  /**
   * Creates an {@link DataInteraction} for a data object displayed by the application. Use this
   * method to load (into the view hierarchy) items from AdapterView widgets (e.g. ListView).
   *
   * @param dataMatcher a matcher used to find the data object.
   */
  public static DataInteraction onData(Matcher<Object> dataMatcher) {
    return new DataInteraction(dataMatcher);
  }

  /**
   * Registers a Looper for idle checking with the framework. This is intended for use with
   * non-UI thread loopers.
   *
   * @throws IllegalArgumentException if looper is the main looper.
   */
  public static void registerLooperAsIdlingResource(Looper looper) {
    registerLooperAsIdlingResource(looper, false);
  }

  /**
   * Registers a Looper for idle checking with the framework. This is intended for use with
   * non-UI thread loopers.
   *
   * This method allows the caller to consider Thread.State.WAIT to be 'idle'.
   *
   * This is useful in the case where a looper is sending a message to the UI thread synchronously
   * through a wait/notify mechanism.
   *
   * @throws IllegalArgumentException if looper is the main looper.
   */
  public static void registerLooperAsIdlingResource(Looper looper, boolean considerWaitIdle) {
    espressoGraph().get(IdlingResourceRegistry.class).registerLooper(looper, considerWaitIdle);
  }

  /**
   * Registers one or more {@link IdlingResource}s with the framework. It is expected, although not
   * strictly required, that this method will be called at test setup time prior to any interaction
   * with the application under test. When registering more than one resource, ensure that each has
   * a unique name.
   */
  public static void registerIdlingResources(IdlingResource... resources) {
    checkNotNull(resources);
    IdlingResourceRegistry registry = espressoGraph().get(IdlingResourceRegistry.class);
    for (IdlingResource resource : resources) {
      checkNotNull(resource.getName(), "IdlingResource.getName() should not be null");
      registry.register(resource);
    }
  }

  /**
   * Changes the default {@link FailureHandler} to the given one.
   */
  public static void setFailureHandler(FailureHandler failureHandler) {
    espressoGraph().get(BaseLayerModule.FailureHandlerHolder.class)
        .update(checkNotNull(failureHandler));
  }

  /********************************** Top Level Actions ******************************************/

  // Ideally, this should be only allOf(isDisplayed(), withContentDescription("More options"))
  // But the ActionBarActivity compat lib is missing a content description for this element, so
  // we add the class name matcher as another option to find the view.
  @SuppressWarnings("unchecked")
  private static final Matcher<View> OVERFLOW_BUTTON_MATCHER = anyOf(
    allOf(isDisplayed(), withContentDescription("More options")), 
    allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton"))));


  /**
   * Closes soft keyboard if open.
   */
  public static void closeSoftKeyboard() {
    onView(isRoot()).perform(ViewActions.closeSoftKeyboard());
  }

  /**
   * Opens the overflow menu displayed in the contextual options of an ActionMode.
   *
   * This works with both native and SherlockActionBar action modes.
   *
   * Note the significant difference in UX between ActionMode and ActionBar overflows - ActionMode
   * will always present an overflow icon and that icon only responds to clicks. The menu button
   * (if present) has no impact on it.
   */
  @SuppressWarnings("unchecked")
  public static void openContextualActionModeOverflowMenu() {
    onView(isRoot())
        .perform(new TransitionBridgingViewAction());

    onView(OVERFLOW_BUTTON_MATCHER)
        .perform(click());
  }

  /**
   * Press on the back button.
   *
   * @throws PerformException if currently displayed activity is root activity, since pressing back
   *         button would result in application closing.
   */
  public static void pressBack() {
    onView(isRoot()).perform(ViewActions.pressBack());
  }

  /**
   * Opens the overflow menu displayed within an ActionBar.
   *
   * This works with both native and SherlockActionBar ActionBars.
   *
   * Note the significant differences of UX between ActionMode and ActionBars with respect to
   * overflows. If a hardware menu key is present, the overflow icon is never displayed in
   * ActionBars and can only be interacted with via menu key presses.
   */
  @SuppressWarnings("unchecked")
  public static void openActionBarOverflowOrOptionsMenu(Context context) {
    if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.HONEYCOMB) {
      // regardless of the os level of the device, this app will be rendering a menukey
      // in the virtual navigation bar (if present) or responding to hardware option keys on
      // any activity.
      onView(isRoot())
          .perform(pressMenuKey());
    } else if (hasVirtualOverflowButton(context)) {
      // If we're using virtual keys - theres a chance we're in mid animation of switching
      // between a contextual action bar and the non-contextual action bar. In this case there
      // are 2 'More Options' buttons present. Lets wait till that is no longer the case.
      onView(isRoot())
          .perform(new TransitionBridgingViewAction());

      onView(OVERFLOW_BUTTON_MATCHER)
          .perform(click());
    } else {
      // either a hardware button exists, or we're on a pre-HC os.
      onView(isRoot())
          .perform(pressMenuKey());
    }
  }

  private static boolean hasVirtualOverflowButton(Context context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    } else {
      return !ViewConfiguration.get(context).hasPermanentMenuKey();
    }
  }

  /**
   * Handles the cases where the app is transitioning between a contextual action bar and a
   * non contextual action bar.
   */
  private static class TransitionBridgingViewAction implements ViewAction {
    @Override
    public void perform(UiController controller, View view) {
      int loops = 0;
      while (isTransitioningBetweenActionBars(view) && loops < 100) {
        loops++;
        controller.loopMainThreadForAtLeast(50);
      }
      // if we're not transitioning properly the next viewaction
      // will give a decent enough exception.
    }

    @Override
    public String getDescription() {
      return "Handle transition between action bar and action bar context.";
    }

    @Override
    public Matcher<View> getConstraints() {
      return isRoot();
    }

    private boolean isTransitioningBetweenActionBars(View view) {
      int actionButtonCount = 0;
      for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
        if (OVERFLOW_BUTTON_MATCHER.matches(child)) {
          actionButtonCount++;
        }
      }
      return actionButtonCount > 1;
    }
  }


}
