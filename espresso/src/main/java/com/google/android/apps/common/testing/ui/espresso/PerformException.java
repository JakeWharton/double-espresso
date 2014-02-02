package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indicates that an exception occurred while performing a ViewAction on the UI thread.
 *
 * A description of the {@link ViewAction}, the view being performed on and the cause are included
 * in the error. Note: {@link FailureHandler}s can mutate the exception later to make it more user
 * friendly.
 *
 * This is generally not recoverable so it is thrown on the instrumentation thread.
 */
public final class PerformException extends RuntimeException implements EspressoException {

  private static final String MESSAGE_FORMAT = "Error performing '%s' on view '%s'.";

  private final String actionDescription;
  private final String viewDescription;

  private PerformException(Builder builder) {
    super(String.format(MESSAGE_FORMAT, builder.actionDescription, builder.viewDescription),
        builder.cause);
    this.actionDescription = checkNotNull(builder.actionDescription);
    this.viewDescription = checkNotNull(builder.viewDescription);
  }

  public String getActionDescription() {
    return actionDescription;
  }

  public String getViewDescription() {
    return viewDescription;
  }

  /**
   * Builder for {@link PerformException}.
   */
  public static class Builder {
    private String actionDescription;
    private String viewDescription;
    private Throwable cause;

    public Builder from(PerformException instance) {
      this.actionDescription = instance.getActionDescription();
      this.viewDescription = instance.getViewDescription();
      this.cause = instance.getCause();
      return this;
    }

    public Builder withActionDescription(String actionDescription) {
      this.actionDescription = actionDescription;
      return this;
    }

    public Builder withViewDescription(String viewDescription) {
      this.viewDescription = viewDescription;
      return this;
    }

    public Builder withCause(Throwable cause) {
      this.cause = cause;
      return this;
    }

    public PerformException build() {
      return new PerformException(this);
    }
  }
}
