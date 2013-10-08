package com.google.android.apps.common.testing.testrunner.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/**
 * Annotates a context as being the TargetContext of an instrumentation.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetContext { }
