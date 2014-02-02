package com.google.android.apps.common.testing.testrunner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates to the test runner that a specific test should not be run on a
 * certain version of android.
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SdkSuppress {

  int[] versions() default {};
  int bugId();

}
