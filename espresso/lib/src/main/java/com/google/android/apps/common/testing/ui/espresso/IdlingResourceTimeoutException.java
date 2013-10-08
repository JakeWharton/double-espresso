package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

/**
 * Indicates that an {@link IdlingResource}, which has been registered with the framework, has not
 * idled within the allowed time.
 *
 * Since it is not safe to proceed with test execution while the registered resource is busy (as it
 * is likely to cause inconsistent results in the test), this is an unrecoverable error. The test
 * author should verify that the {@link IdlingResource} interface has been implemented correctly.
 */
public final class IdlingResourceTimeoutException extends RuntimeException
    implements EspressoException {

  public IdlingResourceTimeoutException(List<String> resourceNames) {
    super(String.format("Wait for %s to become idle timed out", checkNotNull(resourceNames)));
  }
}
