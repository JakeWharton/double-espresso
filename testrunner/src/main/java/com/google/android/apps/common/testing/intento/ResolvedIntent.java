package com.google.android.apps.common.testing.intento;

import android.content.Intent;

/**
 * An {@link Intent} that has been processed to determine the set of packages to which it resolves.
 */
public interface ResolvedIntent {

  /**
   * Returns {@code true} if this recorded intent can be handled by an activity in the given
   * package.
   */
  public boolean canBeHandledBy(String appPackage);
}
