package com.google.android.apps.common.testing.ui.espresso.contrib;

import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Hamcrest matchers for a {@link DrawerLayout}.
 */
public final class DrawerMatchers {

  private DrawerMatchers() {
    // forbid instantiation
  }

  /**
   * Returns a matcher that verifies that the drawer is open. Matches only when the drawer is fully
   * open. Use {@link #isClosed()} instead of {@code not(isOpen())} when you wish to check that the
   * drawer is fully closed.
   */
  public static Matcher<View> isOpen() {
    return new BoundedMatcher<View, DrawerLayout>(DrawerLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("is drawer open");
      }

      @Override
      public boolean matchesSafely(DrawerLayout drawer) {
        return drawer.isDrawerOpen(GravityCompat.START);
      }
    };
  }

  /**
   * Returns a matcher that verifies that the drawer is closed. Matches only when the drawer is
   * fully closed. Use {@link #isOpen()} instead of {@code not(isClosed()))} when you wish to check
   * that the drawer is fully open.
   */
  public static Matcher<View> isClosed() {
    return new BoundedMatcher<View, DrawerLayout>(DrawerLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("is drawer closed");
      }

      @Override
      public boolean matchesSafely(DrawerLayout drawer) {
        return !drawer.isDrawerVisible(GravityCompat.START);
      }
    };
  }
}
