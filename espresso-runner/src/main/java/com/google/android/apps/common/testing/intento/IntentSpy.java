package com.google.android.apps.common.testing.intento;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;


import java.util.List;

/**
 * A sneaky singleton object used to record intents and response to intents with fake responses.
 * This interface is not meant for public consumption. Test authors should use {@Intento} instead.
 */
public interface IntentSpy {

  /**
   * Returns an ImmutableList of intents that have been launched by the application since the last
   * time this method was called (this method clears IntentSpy intent cache every time it is
   * called). Note: the return type is not ImmutableList to avoid bringing in common deps into the
   * test runner.
   */
  public List<ResolvedIntent> getLatestRecordedIntents();

  /**
   * Records the given intent.
   */
  public void record(Intent intent);

  /**
   * Returns the first matching stubbed result for the given activity if stubbed result was set by
   * test author using the {@link #setActivityResultForIntent(Matcher, ActivityResult)} method. The
   * method searches the list of existing matcher/response pairs in the order in which they were
   * entered. If no stubbed result matching the given intent is found, a default RESULT_OK result
   * with null data is returned.
   */
  public ActivityResult getActivityResultForIntent(Intent intent);

  /**
   * Returns {@code true} if intent should proceed and reach the destination activity.
   */
  public boolean allowIntentToProceed(Intent intent);

  /**
   * Clears state (recorded intents, expected responses, etc).
   */
  public void reset();

}
