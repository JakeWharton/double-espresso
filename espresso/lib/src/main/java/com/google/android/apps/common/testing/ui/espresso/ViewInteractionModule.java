package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.base.ViewFinderImpl;

import android.view.View;

import dagger.Module;
import dagger.Provides;

import org.hamcrest.Matcher;

/**
 * Adds the user interaction scope to the Espresso graph.
 */
@Module(
    addsTo = Espresso.EspressoModule.class,
    injects = {ViewInteraction.class})
class ViewInteractionModule {

  private final Matcher<View> viewMatcher;

  ViewInteractionModule(Matcher<View> viewMatcher) {
    this.viewMatcher = checkNotNull(viewMatcher);
  }

  @Provides
  Matcher<View> provideViewMatcher() {
    return viewMatcher;
  }

  @Provides
  ViewFinder provideViewFinder(ViewFinderImpl impl) {
    return impl;
  }
}
