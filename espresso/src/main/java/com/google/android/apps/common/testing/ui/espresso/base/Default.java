package com.google.android.apps.common.testing.ui.espresso.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Annotates a default provider.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
}
