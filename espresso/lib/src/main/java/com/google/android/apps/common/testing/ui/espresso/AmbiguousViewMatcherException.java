package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception which indicates that a Matcher<View> matched multiple views in the hierarchy when
 * only one view was expected.
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
public final class AmbiguousViewMatcherException extends RuntimeException
    implements EspressoException {

  private AmbiguousViewMatcherException(String description) {
    super(description);
  }

  /**
   * Creates a new AmbiguousViewMatcherException suitable for erroring out a test case.
   *
   *  This should be called only on the UI Thread since it will create a dump of the view hierarchy
   * into the exception description.
   *
   * @param viewMatcher the matcher used to traverse the view.
   * @param rootView the root of the view hierarchy.
   * @param view1 the first ambiguous view
   * @param view2 the second ambiguous view
   * @param others any other ambiguous views
   * @return a AmbiguousViewMatcherException suitable to be thrown on the instrumentation thread.
   */
  public static AmbiguousViewMatcherException create(Matcher<? super View> viewMatcher,
      final View rootView, final View view1, final View view2, final View... others) {
    checkNotNull(viewMatcher);
    checkNotNull(rootView);
    checkNotNull(view1);
    checkNotNull(view2);
    checkNotNull(others);

    final ImmutableSet<View> ambiguousViews =
        ImmutableSet.<View>builder().add(view1, view2).add(others).build();

    String errorMessage = HumanReadables.getViewHierarchyErrorMessage(rootView,
        Optional.of((List<View>) new ArrayList<View>(ambiguousViews)),
        String.format("'%s' matches multiple views in the hierarchy.", viewMatcher),
        Optional.of("****MATCHES****"));

    return new AmbiguousViewMatcherException(errorMessage);
  }
}
