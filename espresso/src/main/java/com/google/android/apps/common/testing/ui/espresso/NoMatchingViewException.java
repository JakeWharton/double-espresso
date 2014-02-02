package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

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

  private Matcher<? super View> viewMatcher;
  private View rootView;
  private List<View> adapterViews = Lists.newArrayList();
  private boolean includeViewHierarchy = true;
  private Optional<String> adapterViewWarning = Optional.<String>absent();

  private NoMatchingViewException(String description) {
    super(description);
  }

  private NoMatchingViewException(Builder builder) {
    super(getErrorMessage(builder));
    this.viewMatcher = builder.viewMatcher;
    this.rootView = builder.rootView;
    this.adapterViews = builder.adapterViews;
    this.adapterViewWarning = builder.adapterViewWarning;
    this.includeViewHierarchy = builder.includeViewHierarchy;
  }

  private static String getErrorMessage(Builder builder) {
    String errorMessage = "";
    if (builder.includeViewHierarchy) {
      Optional<List<View>> problemViews = Optional.absent();
      Optional<String> problemViewSuffix = Optional.absent();
      String message = String.format("No views in hierarchy found matching: %s",
          builder.viewMatcher);
      if (builder.adapterViewWarning.isPresent()) {
        message = message + builder.adapterViewWarning.get();
      }
      errorMessage = HumanReadables.getViewHierarchyErrorMessage(builder.rootView, problemViews,
          message, problemViewSuffix);
    } else {
      errorMessage = String.format("Could not find a view that matches %s" , builder.viewMatcher);
    }
    return errorMessage;
  }

  /** Builder for {@link NoMatchingViewException}. */
  public static class Builder {

    private Matcher<? super View> viewMatcher;
    private View rootView;
    private List<View> adapterViews = Lists.newArrayList();
    private boolean includeViewHierarchy = true;
    private Optional<String> adapterViewWarning = Optional.<String>absent();

    public Builder from(NoMatchingViewException exception) {
      this.viewMatcher = exception.viewMatcher;
      this.rootView = exception.rootView;
      this.adapterViews = exception.adapterViews;
      this.adapterViewWarning = exception.adapterViewWarning;
      this.includeViewHierarchy = exception.includeViewHierarchy;
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

    public Builder withAdapterViews(List<View> adapterViews) {
      this.adapterViews = adapterViews;
      return this;
    }

    public Builder includeViewHierarchy(boolean includeViewHierarchy) {
      this.includeViewHierarchy = includeViewHierarchy;
      return this;
    }

    public Builder withAdapterViewWarning(Optional<String> adapterViewWarning) {
      this.adapterViewWarning = adapterViewWarning;
      return this;
    }

    public NoMatchingViewException build() {
      checkNotNull(viewMatcher);
      checkNotNull(rootView);
      checkNotNull(adapterViews);
      checkNotNull(adapterViewWarning);
      return new NoMatchingViewException(this);
    }
  }
}
