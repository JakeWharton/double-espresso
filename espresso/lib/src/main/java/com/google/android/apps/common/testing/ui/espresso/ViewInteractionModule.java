package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.base.RootViewPicker;
import com.google.android.apps.common.testing.ui.espresso.base.ViewFinderImpl;
import com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers;

import android.view.View;

import dagger.Module;
import dagger.Provides;

import org.hamcrest.Matcher;

/**
 * Adds the user interaction scope to the Espresso graph.
 */
@Module(
    addsTo = GraphHolder.EspressoModule.class,
    injects = {ViewInteraction.class})
class ViewInteractionModule {

  private final Matcher<View> viewMatcher;
  private final Matcher<Root> rootMatcher;

  ViewInteractionModule(Matcher<View> viewMatcher) {
    this(viewMatcher, RootMatchers.DEFAULT);
  }

  ViewInteractionModule(Matcher<View> viewMatcher, Matcher<Root> rootMatcher) {
    this.viewMatcher = checkNotNull(viewMatcher);
    this.rootMatcher = checkNotNull(rootMatcher);
  }

  @Provides
  Matcher<Root> provideRootMatcher() {
    return rootMatcher;
  }

  @Provides
  Matcher<View> provideViewMatcher() {
    return viewMatcher;
  }

  @Provides
  ViewFinder provideViewFinder(ViewFinderImpl impl) {
    return impl;
  }

  @Provides
  public View provideRootView(RootViewPicker rootViewPicker) {
    // RootsOracle acts as a provider, but returning Providers is illegal, so delegate.
    return rootViewPicker.get();
  }
}
