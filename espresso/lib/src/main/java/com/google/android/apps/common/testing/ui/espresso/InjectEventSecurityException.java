package com.google.android.apps.common.testing.ui.espresso;

/**
 * An checked {@link Exception} indicating that event injection failed with a
 * {@link SecurityException}.
 */
public final class InjectEventSecurityException extends Exception implements EspressoException {

  public InjectEventSecurityException(String message) {
    super(message);
  }

  public InjectEventSecurityException(Throwable cause) {
    super(cause);
  }

  public InjectEventSecurityException(String message, Throwable cause) {
    super(message, cause);
  }
}
