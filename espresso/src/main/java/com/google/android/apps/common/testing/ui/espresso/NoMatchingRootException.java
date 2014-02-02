package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import org.hamcrest.Matcher;

import java.util.List;

/**
 * Indicates that a given matcher did not match any {@link Root}s (windows) from those that are
 * currently available.
 */
public final class NoMatchingRootException extends RuntimeException implements EspressoException {

  private NoMatchingRootException(String description) {
    super(description);
  }

  public static NoMatchingRootException create(Matcher<Root> rootMatcher, List<Root> roots) {
    checkNotNull(rootMatcher);
    checkNotNull(roots);
    return new NoMatchingRootException(String.format(
        "Matcher '%s' did not match any of the following roots: %s", rootMatcher, roots));
  }
}
