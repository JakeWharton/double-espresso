package com.google.android.apps.common.testing.ui.espresso;

/**
 * An exception which indicates that there are no activities in stage RESUMED.
 */
public final class NoActivityResumedException extends RuntimeException
    implements EspressoException {
  public NoActivityResumedException(String description) {
    super(description);
  }

  public NoActivityResumedException(String description, Throwable cause) {
    super(description, cause);
  }
}
