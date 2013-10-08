package com.google.android.apps.common.testing.ui.espresso.base;

import com.google.android.apps.common.testing.ui.espresso.IdlingResource;

/**
 * An {@link IdlingResource} for testing that becomes idle on demand.
 */
public class OnDemandIdlingResource implements IdlingResource {
  private final String name;

  private boolean isIdle = false;
  private ResourceCallback callback;

  public OnDemandIdlingResource(String name) {
    this.name = name;
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.callback = callback;
  }

  @Override
  public boolean isIdleNow() {
    return isIdle;
  }

  @Override
  public String getName() {
    return name;
  }

  public void forceIdleNow() {
    isIdle = true;
    if (callback != null) {
      callback.onTransitionToIdle();
    }
  }

  public void reset() {
    isIdle = false;
  }
}
