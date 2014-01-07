package com.google.android.apps.common.testing.ui.espresso.base;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitor;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.InstrumentationRegistry;
import com.google.android.apps.common.testing.testrunner.inject.TargetContext;
import com.google.android.apps.common.testing.ui.espresso.FailureHandler;
import com.google.android.apps.common.testing.ui.espresso.Root;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.common.base.Optional;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import dagger.Module;
import dagger.Provides;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dagger module for creating the implementation classes within the base package.
 */
@Module(library = true, injects = {
    BaseLayerModule.FailureHandlerHolder.class, FailureHandler.class})
public class BaseLayerModule {

  @Provides @Singleton
  public ActivityLifecycleMonitor provideLifecycleMonitor() {
    // TODO(user): replace with installation of AndroidInstrumentationModule once
    // proguard issues resolved.
    return ActivityLifecycleMonitorRegistry.getInstance();
  }

  @Provides @TargetContext
  public Context provideTargetContext() {
    // TODO(user): replace with installation of AndroidInstrumentationModule once
    // proguard issues resolved.
    return InstrumentationRegistry.getInstance().getTargetContext();
  }

  @Provides @Singleton
  public Looper provideMainLooper() {
    return Looper.getMainLooper();
  }

  @Provides
  public UiController provideUiController(UiControllerImpl uiControllerImpl) {
    return uiControllerImpl;
  }

  @Provides @Singleton @CompatAsyncTask
  public Optional<AsyncTaskPoolMonitor> provideCompatAsyncTaskMonitor(
      ThreadPoolExecutorExtractor extractor) {
    Optional<ThreadPoolExecutor> compatThreadPool = extractor.getCompatAsyncTaskThreadPool();
    if (compatThreadPool.isPresent()) {
      return Optional.of(new AsyncTaskPoolMonitor(compatThreadPool.get()));
    } else {
      return Optional.<AsyncTaskPoolMonitor>absent();
    }
  }

  @Provides @Singleton @MainThread
  public Executor provideMainThreadExecutor(Looper mainLooper) {
    final Handler handler = new Handler(mainLooper);
    return new Executor() {
      @Override
      public void execute(Runnable runnable) {
        handler.post(runnable);
      }
    };
  }

  @Provides @Singleton @SdkAsyncTask
  public AsyncTaskPoolMonitor provideSdkAsyncTaskMonitor(ThreadPoolExecutorExtractor extractor) {
    return new AsyncTaskPoolMonitor(extractor.getAsyncTaskThreadPool());

  }

  @Provides
  public List<Root> provideKnownRoots(RootsOracle rootsOracle) {
    // RootsOracle acts as a provider, but returning Providers is illegal, so delegate.
    return rootsOracle.get();
  }

  @Provides @Singleton
  public EventInjector provideEventInjector() {
    // On API 16 and above, android uses input manager to inject events. On API < 16,
    // they use Window Manager. So we need to create our InjectionStrategy depending on the api
    // level. Instrumentation does not check if the event presses went through by checking the
    // boolean return value of injectInputEvent, which is why we created this class to better
    // handle lost/dropped press events. Instrumentation cannot be used as a fallback strategy,
    // since this will be executed on the main thread.
    int sdkVersion = Build.VERSION.SDK_INT;
    EventInjectionStrategy injectionStrategy = null;
    if (sdkVersion >= 16) { // Use InputManager for API level 16 and up.
      InputManagerEventInjectionStrategy strategy = new InputManagerEventInjectionStrategy();
      strategy.initialize();
      injectionStrategy = strategy;
    } else if (sdkVersion >= 7) {
      // else Use WindowManager for API level 15 through 7.
      WindowManagerEventInjectionStrategy strategy = new WindowManagerEventInjectionStrategy();
      strategy.initialize();
      injectionStrategy = strategy;
    } else {
      throw new RuntimeException(
          "API Level 6 and below is not supported. You are running: " + sdkVersion);
    }
    return new EventInjector(injectionStrategy);
  }

  /**
   * Holder for AtomicReference<FailureHandler> which allows updating it at runtime.
   */
  @Singleton
  public static class FailureHandlerHolder {
    private final AtomicReference<FailureHandler> holder;

    @Inject
    public FailureHandlerHolder(@Default FailureHandler defaultHandler) {
      holder = new AtomicReference<FailureHandler>(defaultHandler);
    }

    public void update(FailureHandler handler) {
      holder.set(handler);
    }

    public FailureHandler get() {
      return holder.get();
    }
  }

  @Provides
  FailureHandler provideFailureHandler(FailureHandlerHolder holder) {
    return holder.get();
  }

  @Provides
  @Default
  FailureHandler provideFailureHander(DefaultFailureHandler impl) {
    return impl;
  }

}
