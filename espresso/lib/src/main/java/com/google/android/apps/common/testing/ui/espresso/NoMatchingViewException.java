package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import android.view.View;

import org.hamcrest.Matcher;

import java.util.List;

/**
 * Indicates that a given matcher did not match any elements in the view hierarchy.
 * <p>
 * Contains details about the matcher and the current view hierarchy to aid in debugging.
 * </p>
 * <p>
 * Since this is usually an unrecoverable error this exception is a runtime exception.
 * </p>
 * <p>
 * References to the view and failing matcher are purposefully not included in the state of this
 * object - since it will most likely be created on the UI thread and thrown on the instrumentation
 * thread, it would be invalid to touch the view on the instrumentation thread. Also the view
 * hierarchy may have changed since exception creation (leading to more confusion).
 * </p>
 */
public final class NoMatchingViewException extends RuntimeException implements EspressoException {

  private NoMatchingViewException(String description) {
    super(description);
  }

  /**
   * Creates a new {@link NoMatchingViewException} suitable for erroring out a test case.
   *
   * This should be called only on the UI Thread since it will create a dump of the view hierarchy
   * into the exception description.
   *
   * @param viewMatcher the matcher used to traverse the view.
   * @param rootView the root of the view hierarchy.
   * @return a NoMatchingViewException suitable to be thrown on the instrumentation thread.
   */
  public static NoMatchingViewException create(
      Matcher<? super View> viewMatcher, final View rootView) {
    return create(Optional.<String>absent(), viewMatcher, rootView);
  }

  /**
   * Same as {@link #create(Matcher, View)}, but with additional logging about AdapterViews.
   */
  public static NoMatchingViewException create(
      Matcher<? super View> viewMatcher, final View rootView, final List<View> adapterViews) {
    checkNotNull(adapterViews);
    if (adapterViews.isEmpty()) {
      return create(viewMatcher, rootView);
    }
    String warning = String.format("\nIf the target view is not part of the view hierarchy, you "
        + "may need to use Espresso.onData to load it from one of the following AdapterViews:%s"
        , Joiner.on("\n- ").join(adapterViews));
    return create(Optional.of(warning), viewMatcher, rootView);
  }

  private static NoMatchingViewException create(
      Optional<String> adapterViewWarning, Matcher<? super View> viewMatcher, final View rootView) {
    checkNotNull(viewMatcher);
    checkNotNull(rootView);
    Optional<List<View>> problemViews = Optional.absent();
    Optional<String> problemViewSuffix = Optional.absent();
    // TODO(user): move printing out of the view hierarchy into the DefaultFailureHandler so that
    // the verbose output can be overridden by projects that don't need it (e.g. Corretto).
    String message = String.format("No views in hierarchy found matching: %s", viewMatcher);
    if (adapterViewWarning.isPresent()) {
      message = message + adapterViewWarning.get();
    }
    String errorMessage = HumanReadables.getViewHierarchyErrorMessage(rootView, problemViews,
        message, problemViewSuffix);

    return new NoMatchingViewException(errorMessage);
  }
}
