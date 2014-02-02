package com.google.android.apps.common.testing.testrunner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates to the test runner that a specific test should not be run on emulator.
 * It will be executed only if the test is running on the physical android device.
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresDevice {
}
