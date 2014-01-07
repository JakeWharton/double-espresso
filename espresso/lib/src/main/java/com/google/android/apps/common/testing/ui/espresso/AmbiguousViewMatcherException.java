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
 * only one view was expected. It should be called only from the main thread.
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

  private Matcher<? super View> viewMatcher;
  private View rootView;
  private View view1;
  private View view2;
  private View[] others;
  private boolean includeViewHierarchy;

  private AmbiguousViewMatcherException(String description) {
    super(description);
  }

  private AmbiguousViewMatcherException(Builder builder) {
    super(getErrorMessage(builder));
    this.viewMatcher = builder.viewMatcher;
    this.rootView = builder.rootView;
    this.view1 = builder.view1;
    this.view2 = builder.view2;
    this.others = builder.others;
  }

  private static String getErrorMessage(Builder builder) {
    String errorMessage = "";
    if (builder.includeViewHierarchy) {
      ImmutableSet<View> ambiguousViews =
        ImmutableSet.<View>builder().add(builder.view1, builder.view2).add(builder.others).build();
      errorMessage = HumanReadables.getViewHierarchyErrorMessage(builder.rootView,
          Optional.of((List<View>) new ArrayList<View>(ambiguousViews)),
          String.format("'%s' matches multiple views in the hierarchy.", builder.viewMatcher),
          Optional.of("****MATCHES****"));
    } else {
      errorMessage = String.format("Multiple Ambiguous Views found for matcher %s",
          builder.viewMatcher);
    }
    return errorMessage;
  }

  /** Builder for {@link AmbiguousViewMatcherException}. */
  public static class Builder {
    private Matcher<? super View> viewMatcher;
    private View rootView;
    private View view1;
    private View view2;
    private View[] others;
    private boolean includeViewHierarchy = true;

    public Builder from(AmbiguousViewMatcherException exception) {
      this.viewMatcher = exception.viewMatcher;
      this.rootView = exception.rootView;
      this.view1 = exception.view1;
      this.view2 = exception.view2;
      this.others = exception.others;
      return this;
    }

    public Builder withViewMatcher(Matcher<? super View> viewMatcher) {
      this.viewMatcher = viewMatcher;
      return this;
    }

    public Builder withRootView(View rootView) {
      this.rootView = rootView;
      return this;
    }

    public Builder withView1(View view1) {
      this.view1 = view1;
      return this;
    }

    public Builder withView2(View view2) {
      this.view2 = view2;
      return this;
    }

    public Builder withOtherAmbiguousViews(View... others) {
      this.others = others;
      return this;
    }

    public Builder includeViewHierarchy(boolean includeViewHierarchy) {
      this.includeViewHierarchy = includeViewHierarchy;
      return this;
    }

    public AmbiguousViewMatcherException build() {
      checkNotNull(viewMatcher);
      checkNotNull(rootView);
      checkNotNull(view1);
      checkNotNull(view2);
      checkNotNull(others);
      return new AmbiguousViewMatcherException(this);
    }
  }
}
