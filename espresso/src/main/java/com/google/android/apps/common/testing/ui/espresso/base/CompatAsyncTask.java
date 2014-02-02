package com.google.android.apps.common.testing.ui.espresso.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Annotates a AsyncTaskMonitor as monitoring the CompatAsyncTask pool
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@interface CompatAsyncTask { }
