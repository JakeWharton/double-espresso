package com.google.android.apps.common.testing.testrunner;

import com.google.android.apps.common.testing.intento.IntentSpy;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instrumentation that enables several advanced features and makes some hard guarantees about
 * the state of the application under instrumentation.
 *
 * A short list of these capabilities:
 *   - Forces Application.onCreate() to happen before Instrumentation.onStart() runs (ensuring your
 *     code always runs in a sane state).
 *   - Logs application death due to exceptions.
 *   - Allows tracking of activity lifecycle states.
 *   - Registers instrumentation arguments in an easy to access place.
 *   - Ensures your activites are creating themselves in reasonable amounts of time.
 *   - Provides facilties to dump current app threads to test outputs.
 *   - Ensures all activities finish before instrumentation exits.
 * This Instrumentation is *NOT* a test instrumentation (some of its subclasses are). It makes no
 * assumptions about what the subclass wants to do.
 *
 */
public class
GoogleInstrumentation
extends ExposedInstrumentationApi {

  private static final long MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP = TimeUnit.SECONDS.toMillis(2);
  private static final long MILLIS_TO_POLL_FOR_ACTIVITY_STOP =
      MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP / 40;

  private static final String LOG_TAG = "GoogleInstr";

  private static final String DEFAULT_EMULATOR_IME = "com.android.inputmethod.latin/.LatinIME";
  private static final String DEFAULT_PHONE_IME =
      "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME";
  private static final Locale DEFAULT_LOCALE = Locale.US;

  private ActivityLifecycleMonitorImpl lifecycleMonitor = new ActivityLifecycleMonitorImpl();
  private ExecutorService executorService;
  private Handler handlerForMainLooper;
  private int startActivityTimeoutSeconds = 45;
  private AtomicBoolean anActivityHasBeenLaunched = new AtomicBoolean(false);
  private Thread mainThread;
  private AtomicLong lastIdleTime = new AtomicLong(0);
  private AtomicInteger startedActivityCounter = new AtomicInteger(0);

  private IdleHandler idleHandler = new IdleHandler() {
    @Override
    public boolean queueIdle() {
      lastIdleTime.set(System.currentTimeMillis());
      return true;
    }
  };
  private volatile boolean finished = false;
  private IntentSpy intentSpy = null;


  /**
   * Sets up intent spying, lifecycle monitoring, and argument registry.
   *
   * Subclasses must call up to onCreate(). This oncreate method does not call start()
   * it is the subclasses responsibility to call start if it desires.
   */
  @Override
  public void onCreate(Bundle arguments) {
    Log.i(LOG_TAG, "Instrumentation Started!");
    tryLoadingIntentSpy();
    InstrumentationRegistry.registerInstance(this);
    ActivityLifecycleMonitorRegistry.registerInstance(lifecycleMonitor);

    handlerForMainLooper = new Handler(Looper.getMainLooper());
    mainThread = Thread.currentThread();
    executorService = Executors.newCachedThreadPool();
    Looper.myQueue().addIdleHandler(idleHandler);
    super.onCreate(arguments);
  }

  protected final void specifyDexMakerCacheProperty() {
    // DexMaker uses heuristics to figure out where to store its temporary dex files
    // these heuristics may break (eg - they no longer work on JB MR2). So we create
    // our own cache dir to be used if the app doesnt specify a cache dir, rather then
    // relying on heuristics.
    //

    File dexCache = getTargetContext().getDir("dxmaker_cache", Context.MODE_PRIVATE);
    System.getProperties().put("dexmaker.dexcache", dexCache.getAbsolutePath());
  }

  private void tryLoadingIntentSpy() {
    // Wouldn't it be easier to call IntentSpyImpl.getInstance() and be done with it? We don't do
    // this to avoid bringing in common lib dependencies (which cause painful conflicts with some
    // android projects) into G3ITR. Instead, we try to load IntentSpyImpl via reflection. Projects
    // that don't have intento in deps will not have IntentSpyImpl included at runtime (i.e. we
    // proceed as normally) and leave intentSpy as null (to be checked later). However, if it is
    // loaded, we can call any method of IntentSpy.
    try {
      Class<?> c = Class.forName("com.google.android.apps.common.testing.intento.IntentSpyImpl");
      intentSpy = (IntentSpy) c.getMethod(
          "load", Context.class, Context.class).invoke(null, getTargetContext(), getContext());
      Log.i(LOG_TAG, "IntentSpyImpl loaded");
    } catch (ClassNotFoundException cnfe) {
      Log.i(LOG_TAG, "IntentSpyImpl not loaded: " + cnfe.getMessage());
    } catch (NoSuchMethodException nsme) {
      throw new RuntimeException(
          "IntentSpyImpl is available at runtime, but getInstance method was not found", nsme);
    } catch (SecurityException se) {
      throw new RuntimeException(
          "IntentSpyImpl is available at runtime, but calling it failed.", se);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(
          "IntentSpyImpl is available at runtime, but calling it failed.", iae);
    } catch (IllegalArgumentException iare) {
      throw new RuntimeException(
          "IntentSpyImpl is available at runtime, but calling it failed.", iare);
    } catch (InvocationTargetException ite) {
      throw new RuntimeException(
          "IntentSpyImpl is available at runtime, but calling it failed.", ite);
    }
  }

  /**
   * This implementation of onStart() will guarantee that the Application's onCreate method
   * has completed when it returns.
   * Subclasses should call super.onStart() before executing any code that touches the application
   * and it's state.
   */
  @Override
  public void onStart() {
    super.onStart();

    // Due to the way Android initializes instrumentation - all instrumentations have the
    // possibility of seeing the Application and its classes in an inconsistent state. Specifically
    // ActivityThread creates Instrumentation first, initializes it, and calls
    // instrumentation.onCreate(). After it does that, it calls
    // instrumentation.callApplicationOnCreate() which ends up calling the application's
    // onCreateMethod.
    //
    // So, Android's InstrumentationTestRunner's onCreate method() spawns a separate thread to
    // execute tests. This causes tests to start accessing the application and its classes while
    // the ActivityThread is calling callApplicationOnCreate() in its own thread.
    //
    // This makes it possible for tests to see the application in a state that is normally never
    // visible: pre-application.onCreate() and during application.onCreate()).
    //
    // *phew* that sucks! Here we waitForOnIdleSync() to ensure onCreate has completed before we
    // start executing tests.
    waitForIdleSync();
  }

  /**
   * Ensures all activities launched in this instrumentation are finished before the instrumentation
   * exits.
   *
   * Subclasses who override this method should do their finish processing and then call
   * super.finish to invoke this logic. Not waiting for all activities to finish() before exiting
   * can cause device wide instability.
   */
  @Override
  public void finish(int resultCode, Bundle results) {
    if (finished) {
      Log.w(LOG_TAG, "finish called 2x!");
      return;
    } else {
      finished = true;
    }

    handlerForMainLooper.post(new ActivityFinisher());

    long startTime = System.currentTimeMillis();
    waitForActivitiesToComplete();
    long endTime = System.currentTimeMillis();
    Log.i(LOG_TAG, String.format("waitForActivitiesToComplete() took: %sms", endTime - startTime));
    ActivityLifecycleMonitorRegistry.registerInstance(null);
    super.finish(resultCode, results);
  }

  /**
   * Ensures we've onStopped() all activities which were onStarted().
   *
   * According to Activity's contract, the process is not killable between onStart and onStop.
   * Breaking this contract (which finish() will if you let it) can cause bad behaviour (including
   * a full restart of system_server).
   *
   * We give the app 2 seconds to stop all its activities, then we proceed.
   */
  protected void waitForActivitiesToComplete() {
    long endTime = System.currentTimeMillis() + MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP;
    int currentActivityCount = startedActivityCounter.get();

    while (currentActivityCount > 0 && System.currentTimeMillis() < endTime) {
      try {
        Log.i(LOG_TAG, "Unstopped activity count: " + currentActivityCount);
        Thread.sleep(MILLIS_TO_POLL_FOR_ACTIVITY_STOP);
        currentActivityCount = startedActivityCounter.get();
      } catch (InterruptedException ie) {
        Log.i(LOG_TAG, "Abandoning activity wait due to interruption.", ie);
        break;
      }
    }

    if (currentActivityCount > 0) {
      dumpThreadStateToOutputs("ThreadState-unstopped.txt");
      Log.w(LOG_TAG, String.format("Still %s activities active after waiting %s ms.",
          currentActivityCount, MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP));
    }
  }

  @Override
  public void onDestroy() {
    Log.i(LOG_TAG, "Instrumentation Finished!");
    Looper.myQueue().removeIdleHandler(idleHandler);
    super.onDestroy();
  }


  @Override
  public Activity startActivitySync(final Intent intent) {
    validateNotAppThread();
    long lastIdleTimeBeforeLaunch = lastIdleTime.get();

    if (anActivityHasBeenLaunched.compareAndSet(false, true)) {
      // All activities launched from InsturmentationTestCase.launchActivityWithIntent get
      // started with FLAG_ACTIVITY_NEW_TASK. This includes calls to
      // ActivityInstrumentationTestcase2.getActivity().
      //
      // This gives us a pristine environment - MOST OF THE TIME.
      //
      // However IF we've run a test method previously and that has launched an activity outside
      // of our process our old task is still lingering around. By launching a new activity
      // android will place our activity at the bottom of the stack and bring the previous
      // external activity to the front of the screen.
      //
      // To wipe out the old task and execute within a pristine environment for each test
      // we tell android to CLEAR_TOP the very first activity we see, no matter what.
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    Future<Activity> startedActivity = executorService.submit(new Callable<Activity>() {
      public Activity call() {
return GoogleInstrumentation.super.startActivitySync(intent);
      }
    });

    try {
      return startedActivity.get(startActivityTimeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException te) {
      startedActivity.cancel(true);
      dumpThreadStateToOutputs("ThreadState-startActivityTimeout.txt");
      throw new RuntimeException(String.format("Could not launch intent %s within %s seconds. " +
          "Perhaps the main thread has not gone idle within a reasonable amount of time? There " +
          "could be an animation or something constantly repainting the screen. Or the activity " +
          "is doing network calls on creation? See the threaddump logs. For your reference the " +
          "last time the event queue was idle before your activity launch request was %s and " +
          "and now the last time the queue went idle was: %s. If these numbers are the same " +
          "your activity might be hogging the event queue.",
          intent, startActivityTimeoutSeconds, lastIdleTimeBeforeLaunch, lastIdleTime.get()));
    } catch (ExecutionException ee) {
      throw new RuntimeException("Could not launch activity", ee.getCause());
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("interrupted", ie);
    }
  }

  @Override
  public ActivityResult execStartActivity(
      Context who, IBinder contextThread, IBinder token, Activity target,
      Intent intent, int requestCode) {
    Log.d(LOG_TAG, "execStartActivity(context, ibinder, ibinder, activity, intent, int)");
    if (intentSpy != null) {
      intentSpy.record(intent);
      if (!intentSpy.allowIntentToProceed(intent)) {
        return getResultForBlockedIntent(intent, requestCode);
      }
    }
    return super.execStartActivity(who, contextThread, token, target, intent, requestCode);
  }

  private ActivityResult getResultForBlockedIntent(Intent intent, int requestCode) {
    Log.i(LOG_TAG, "Intento: blocking external intent: " + intent);
    if (requestCode >= 0) {
      // Caller is expecting result.
      ActivityResult result = intentSpy.getActivityResultForIntent(intent);
      Log.i(LOG_TAG, "Intento: returning stub result: " + result);
      return result;
    } else {
      return null;
    }
  }

  @Override
  public ActivityResult execStartActivity(
      Context who, IBinder contextThread, IBinder token, Activity target,
      Intent intent, int requestCode, Bundle options) {
    Log.d(LOG_TAG, "execStartActivity(context, ibinder, ibinder, activity, intent, int, bundle");
    if (intentSpy != null) {
      intentSpy.record(intent);
      if (!intentSpy.allowIntentToProceed(intent)) {
        return getResultForBlockedIntent(intent, requestCode);
      }
    }
    return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  @Override
  public void execStartActivities(Context who, IBinder contextThread,
      IBinder token, Activity target, Intent[] intents, Bundle options)  {
    // This method is used in HONEYCOMB and higher to create a synthetic back stack for the
    // launched activity. The intent at the end of the array is the top most,
    // user visible activity, and the intents beneath it are launched when the user presses back.
    //
    // Intento wants to present this back stack for verification, so we record the synthetic
    // back stack in the order in which it was constructed.
    //
    // TODO(user): How appropriate is this if the virtual back stack is for our own application,
    // does that even make sense.
    Log.d(LOG_TAG, "execStartActivities(context, ibinder, ibinder, activity, intent[], bundle)");
    // For requestCode < 0, the caller doesn't expect any result and
    // in this case we are not expecting any result so selecting
    // a value < 0.
    int requestCode = -1;
    for (int idx = intents.length - 1; idx >= 0; idx--) {
      execStartActivity(who, contextThread, token, target, intents[idx], requestCode, options);
    }
  }

  @Override
  public ActivityResult execStartActivity(
      Context who, IBinder contextThread, IBinder token, Fragment target,
      Intent intent, int requestCode, Bundle options) {
    Log.d(LOG_TAG, "execStartActivity(context, IBinder, IBinder, Fragment, Intent, int, Bundle)");
    if (intentSpy != null) {
      intentSpy.record(intent);
      if (!intentSpy.allowIntentToProceed(intent)) {
        return getResultForBlockedIntent(intent, requestCode);
      }
    }
    return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  private void validateNotAppThread() {
    if (mainThread.equals(Thread.currentThread())) {
      throw new RuntimeException("this method cannot be called from the main application thread");
    }
  }

  @Override
  public boolean onException(Object obj, Throwable e) {
    String error = String.format(
        "Exception encountered by: %s. Dumping thread state to outputs and pining for the fjords.",
        obj,
        e);
    Log.e(LOG_TAG, error);
    dumpThreadStateToOutputs("ThreadState-onException.txt");
    Log.e(LOG_TAG, "Dying now...");
    return super.onException(obj, e);
  }

  protected final void dumpThreadStateToOutputs(String outputFileName) {
    Writer writer = null;
    String threadState = getThreadState();
    Log.e("THREAD_STATE", threadState);
  }


  private static String getThreadState() {
    Set<Map.Entry<Thread, StackTraceElement[]>> threads = Thread.getAllStackTraces().entrySet();
    StringBuilder threadState = new StringBuilder();
    for (Map.Entry<Thread, StackTraceElement[]> threadAndStack : threads) {
      StringBuilder threadMessage = new StringBuilder("  ").append(threadAndStack.getKey());
      threadMessage.append("\n");
      for (StackTraceElement ste : threadAndStack.getValue()) {
        threadMessage.append("    ");
        threadMessage.append(ste.toString());
        threadMessage.append("\n");
      }
      threadMessage.append("\n");
      threadState.append(threadMessage.toString());
    }
    return threadState.toString();
  }



  @Override
  public void callActivityOnDestroy(Activity activity) {
    super.callActivityOnDestroy(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.DESTROYED, activity);
  }

  @Override
  public void callActivityOnRestart(Activity activity) {
    super.callActivityOnRestart(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.RESTARTED, activity);
  }

  @Override
  public void callActivityOnCreate(Activity activity, Bundle bundle) {
    lifecycleMonitor.signalLifecycleChange(Stage.PRE_ON_CREATE, activity);
    super.callActivityOnCreate(activity, bundle);
    lifecycleMonitor.signalLifecycleChange(Stage.CREATED, activity);
  }

  // NOTE: we need to keep a count of activities between the start
  // and stop lifecycle internal to our instrumentation. Exiting the test
  // process with activities in this state can cause crashes/flakiness
  // that would impact a subsequent test run.
  @Override
  public void callActivityOnStart(Activity activity) {
    startedActivityCounter.incrementAndGet();
    try {
      super.callActivityOnStart(activity);
      lifecycleMonitor.signalLifecycleChange(Stage.STARTED, activity);
    } catch (RuntimeException re) {
      startedActivityCounter.decrementAndGet();
      throw re;
    }
  }

  @Override
  public void callActivityOnStop(Activity activity) {
    try {
      super.callActivityOnStop(activity);
      lifecycleMonitor.signalLifecycleChange(Stage.STOPPED, activity);
    } finally {
      startedActivityCounter.decrementAndGet();
    }
  }

  @Override
  public void callActivityOnResume(Activity activity) {
    super.callActivityOnResume(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.RESUMED, activity);
  }

  @Override
  public void callActivityOnPause(Activity activity) {
    super.callActivityOnPause(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.PAUSED, activity);
  }


  /**
   * Loops through all the activities that have not yet finished and explicitly calls finish
   * on them.
   */
  public class ActivityFinisher implements Runnable {
    @Override
    public void run() {
      List<Activity> activities = new ArrayList<Activity>();

      for (Stage s : EnumSet.range(Stage.CREATED, Stage.PAUSED)) {
        activities.addAll(lifecycleMonitor.getActivitiesInStage(s));
      }

      Log.i(LOG_TAG, "Activities that are still in CREATED to PAUSED: " + activities.size());

      for (Activity activity : activities) {
        if (!activity.isFinishing()) {
          try {
            Log.i(LOG_TAG, "Stopping activity: " + activity);
            activity.finish();
          } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Failed to stop activity.", e);
          }
        }
      }
    }
  };
}
