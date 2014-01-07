package com.google.android.apps.common.testing.ui.espresso;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TestSuite containing "unit tests" for the UI Framework.
 *
 */
public class UnitTests extends TestSuite {
  public static Test suite() {
    return new TestSuiteBuilder(UnitTests.class)
      .includeAllPackagesUnderHere()
      .build();
  }
}
