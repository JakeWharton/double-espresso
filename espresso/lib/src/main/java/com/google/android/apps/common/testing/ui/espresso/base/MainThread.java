package com.google.android.apps.common.testing.ui.espresso.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Annotates an Executor that executes tasks on the main thread
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface MainThread { }
