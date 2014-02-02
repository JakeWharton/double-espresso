package com.google.android.apps.common.testing.testrunner;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;


/**
 * Exposes select hidden android apis to the compiler.
 * These methods are stripped from the android.jar sdk compile time jar, however
 * are called at runtime (and exist in the android.jar on the device).
 *
 * This class is built with neverlink=1 to ensure it is never actually included in
 * our apk.
 */
public abstract class ExposedInstrumentationApi extends Instrumentation {
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
          throw new RuntimeException();
    }

    public void execStartActivities(Context who, IBinder contextThread,
            IBinder token, Activity target, Intent[] intents, Bundle options) {
          throw new RuntimeException();
    }

    public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, Fragment target,
        Intent intent, int requestCode, Bundle options) {
          throw new RuntimeException();
    }

  public ActivityResult execStartActivity(
      Context who, IBinder contextThread, IBinder token, Activity target,
      Intent intent, int requestCode) {
          throw new RuntimeException();
  }

}
