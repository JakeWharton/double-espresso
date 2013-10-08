package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;

import android.view.View;
import android.view.WindowManager;

/**
 * Represents a root view in the application and optionally the layout params of the window holding
 * it.
 *
 * This class is used internally to determine which view root to run user provided matchers against
 * it is not part of the public api.
 */
public final class Root {
  private final View decorView;
  private final Optional<WindowManager.LayoutParams> windowLayoutParams;

  private Root(Builder builder) {
    this.decorView = checkNotNull(builder.decorView);
    this.windowLayoutParams = Optional.fromNullable(builder.windowLayoutParams);
  }

  public View getDecorView() {
    return decorView;
  }

  public Optional<WindowManager.LayoutParams> getWindowLayoutParams() {
    return windowLayoutParams;
  }

  @Override
  public String toString() {
    ToStringHelper helper = toStringHelper(this)
        .add("application-window-token", decorView.getApplicationWindowToken())
        .add("window-token", decorView.getWindowToken())
        .add("has-window-focus", decorView.hasWindowFocus());
    if (windowLayoutParams.isPresent()) {
      helper
          .add("layout-params-type", windowLayoutParams.get().type)
          .add("layout-params-string", windowLayoutParams.get());
    }
    helper
        .add("decor-view-string", HumanReadables.describe(decorView));
    return helper.toString();
  }

  public static class Builder {
    private View decorView;
    private WindowManager.LayoutParams windowLayoutParams;

    public Root build() {
      return new Root(this);
    }

    public Builder withDecorView(View view) {
      this.decorView = view;
      return this;
    }

    public Builder withWindowLayoutParams(WindowManager.LayoutParams windowLayoutParams) {
      this.windowLayoutParams = windowLayoutParams;
      return this;
    }
  }
}
