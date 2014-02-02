package com.google.android.apps.common.testing.testrunner.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitor;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.InstrumentationRegistry;

import android.app.Instrumentation;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module(library = true)
public class AndroidInstrumentationModule {

  private final ActivityLifecycleMonitor lifecycleMonitor;
  private final Instrumentation instrumentation;

  public AndroidInstrumentationModule() {
    this(ActivityLifecycleMonitorRegistry.getInstance(),
        InstrumentationRegistry.getInstance());
  }

  public AndroidInstrumentationModule(ActivityLifecycleMonitor lifecycleMonitor,
      Instrumentation instrumentation) {
    this.lifecycleMonitor = checkNotNull(lifecycleMonitor);
    this.instrumentation = checkNotNull(instrumentation);
  }

  @Provides
  public ActivityLifecycleMonitor provideLifecycleMonitor() {
    return lifecycleMonitor;
  }

  @Provides
  public Instrumentation provideInstrumentation() {
    return instrumentation;
  }

  @Provides
  @TargetContext
  public Context provideTargetContext(Instrumentation instrumentation) {
    return instrumentation.getTargetContext();
  }

  @Provides
  @InstrumentationContext
  public Context provideInstrumentationContext(Instrumentation instrumentation) {
    return instrumentation.getContext();
  }

}
