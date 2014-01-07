package com.google.android.apps.common.testing.ui.espresso.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Annotates the timeout in seconds for {@link IdlingResourceRegistry}. */
@Retention(RetentionPolicy.RUNTIME)
public @interface IdlingRegistryTimeoutMs {

}
