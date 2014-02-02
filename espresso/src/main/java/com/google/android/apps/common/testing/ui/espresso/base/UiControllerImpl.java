package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.propagate;

import com.google.android.apps.common.testing.ui.espresso.IdlingPolicies;
import com.google.android.apps.common.testing.ui.espresso.IdlingPolicy;
import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import com.google.android.apps.common.testing.ui.espresso.base.QueueInterrogator.QueueState;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of {@link UiController}.
 */
@Singleton
final class UiControllerImpl implements UiController, Handler.Callback {

  private static final String TAG = UiControllerImpl.class.getSimpleName();

  private static final Callable<Void> NO_OP = new Callable<Void>() {
    @Override
    public Void call() {
      return null;
    }
  };

  /**
   * Responsible for signaling a particular condition is met / verifying that signal.
   */
  enum IdleCondition {
      DELAY_HAS_PAST,
      ASYNC_TASKS_HAVE_IDLED,
      COMPAT_TASKS_HAVE_IDLED,
      KEY_INJECT_HAS_COMPLETED,
      MOTION_INJECTION_HAS_COMPLETED,
      DYNAMIC_TASKS_HAVE_IDLED;

      /**
       * Checks whether this condition has been signaled.
       */
      public boolean isSignaled(BitSet conditionSet) {
        return conditionSet.get(ordinal());
      }

      /**
       * Resets the signal state for this condition.
       */
      public void reset(BitSet conditionSet) {
        conditionSet.set(ordinal(), false);
      }

      /**
       * Creates a message that when sent will raise the signal of this condition.
       */
      public Message createSignal(Handler handler, int myGeneration) {
        return Message.obtain(handler, ordinal(), myGeneration, 0, null);
      }

      /**
       * Handles a message that is raising a signal and updates the condition set accordingly.
       * Messages from a previous generation will be ignored.
       */
      public static boolean handleMessage(Message message, BitSet conditionSet,
          int currentGeneration) {
        IdleCondition [] allConditions = values();
        if (message.what < 0 || message.what >= allConditions.length) {
          return false;
        } else {
          IdleCondition condition = allConditions[message.what];
          if (message.arg1 == currentGeneration) {
            condition.signal(conditionSet);
          } else {
            Log.w(TAG, "ignoring signal of: " + condition + " from previous generation: " +
                message.arg1 + " current generation: " + currentGeneration);
          }
          return true;
        }
      }

      public static BitSet createConditionSet() {
        return new BitSet(values().length);
      }

      /**
       * Requests that the given bitset be updated to indicate that this condition has been
       * signaled.
       */
      protected void signal(BitSet conditionSet) {
        conditionSet.set(ordinal());
      }
  }

  private final EventInjector eventInjector;
  private final BitSet conditionSet;
  private final AsyncTaskPoolMonitor asyncTaskMonitor;
  private final Optional<AsyncTaskPoolMonitor> compatTaskMonitor;
  private final IdlingResourceRegistry idlingResourceRegistry;
  private final ExecutorService keyEventExecutor = Executors.newSingleThreadExecutor();
  private final QueueInterrogator queueInterrogator;
  private final Looper mainLooper;

  private Handler controllerHandler;
  // only updated on main thread.
  private boolean looping = false;
  private int generation = 0;

  @VisibleForTesting
  @Inject
  UiControllerImpl(EventInjector eventInjector,
      @SdkAsyncTask AsyncTaskPoolMonitor asyncTaskMonitor,
      @CompatAsyncTask Optional<AsyncTaskPoolMonitor> compatTaskMonitor,
      IdlingResourceRegistry registry,
      Looper mainLooper) {
    this.eventInjector = checkNotNull(eventInjector);
    this.asyncTaskMonitor = checkNotNull(asyncTaskMonitor);
    this.compatTaskMonitor = checkNotNull(compatTaskMonitor);
    this.conditionSet = IdleCondition.createConditionSet();
    this.idlingResourceRegistry = checkNotNull(registry);
    this.mainLooper = checkNotNull(mainLooper);
    this.queueInterrogator = new QueueInterrogator(mainLooper);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean injectKeyEvent(final KeyEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    initialize();
    loopMainThreadUntilIdle();

    FutureTask<Boolean> injectTask = new SignalingTask<Boolean>(
        new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            return eventInjector.injectKeyEvent(event);
          }
        },
        IdleCondition.KEY_INJECT_HAS_COMPLETED,
        generation);

    // Inject the key event.
    keyEventExecutor.submit(injectTask);

    loopUntil(IdleCondition.KEY_INJECT_HAS_COMPLETED);

    try {
      checkState(injectTask.isDone(), "Key injection was signaled - but it wasnt done.");
      return injectTask.get();
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof InjectEventSecurityException) {
        throw (InjectEventSecurityException) ee.getCause();
      } else {
        throw new RuntimeException(ee.getCause());
      }
    } catch (InterruptedException neverHappens) {
      // we only call get() after done() is signaled.
      // we should never block.
      throw new RuntimeException("impossible.", neverHappens);
    }
  }

  @Override
  public boolean injectMotionEvent(final MotionEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    initialize();

    FutureTask<Boolean> injectTask = new SignalingTask<Boolean>(
        new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            return eventInjector.injectMotionEvent(event);
          }
        },
        IdleCondition.MOTION_INJECTION_HAS_COMPLETED,
        generation);
    keyEventExecutor.submit(injectTask);
    loopUntil(IdleCondition.MOTION_INJECTION_HAS_COMPLETED);
    try {
      checkState(injectTask.isDone(), "Key injection was signaled - but it wasnt done.");
      return injectTask.get();
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof InjectEventSecurityException) {
        throw (InjectEventSecurityException) ee.getCause();
      } else {
        throw propagate(ee.getCause() != null ? ee.getCause() : ee);
      }
    } catch (InterruptedException neverHappens) {
      // we only call get() after done() is signaled.
      // we should never block.
      throw propagate(neverHappens);
    } finally {
      loopMainThreadUntilIdle();
    }
  }

  @Override
  public boolean injectString(String str) throws InjectEventSecurityException {
    checkNotNull(str);
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    initialize();

    // No-op if string is empty.
    if (str.length() == 0) {
      Log.w(TAG, "Supplied string is empty resulting in no-op (nothing is typed).");
      return true;
    }

    boolean eventInjected = false;
    KeyCharacterMap keyCharacterMap = getKeyCharacterMap();

    // TODO(user): Investigate why not use (as suggested in javadoc of keyCharacterMap.getEvents):
    // http://developer.android.com/reference/android/view/KeyEvent.html#KeyEvent(long,
    // java.lang.String, int, int)
    KeyEvent[] events = keyCharacterMap.getEvents(str.toCharArray());
    checkNotNull(events, "Failed to get events for string " + str);
    Log.d(TAG, String.format("Injecting string: \"%s\"", str));

    for (KeyEvent event : events) {
      checkNotNull(event, String.format("Failed to get event for character (%c) with key code (%s)",
          event.getKeyCode(), event.getUnicodeChar()));

      eventInjected = false;
      for (int attempts = 0; !eventInjected && attempts < 4; attempts++) {
        attempts++;

        // We have to change the time of an event before injecting it because
        // all KeyEvents returned by KeyCharacterMap.getEvents() have the same
        // time stamp and the system rejects too old events. Hence, it is
        // possible for an event to become stale before it is injected if it
        // takes too long to inject the preceding ones.
        event = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 0);
        eventInjected = injectKeyEvent(event);
      }

      if (!eventInjected) {
        Log.e(TAG, String.format("Failed to inject event for character (%c) with key code (%s)",
            event.getUnicodeChar(), event.getKeyCode()));
        break;
      }
    }

    return eventInjected;
  }

  @SuppressLint("InlinedApi")
  @VisibleForTesting
  @SuppressWarnings("deprecation")
  public static KeyCharacterMap getKeyCharacterMap() {
    KeyCharacterMap keyCharacterMap = null;

    // KeyCharacterMap.VIRTUAL_KEYBOARD is present from API11.
    // For earlier APIs we use KeyCharacterMap.BUILT_IN_KEYBOARD
    if (Build.VERSION.SDK_INT < 11) {
      keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
    } else {
      keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    }
    return keyCharacterMap;
  }


  @Override
  public void loopMainThreadUntilIdle() {
    initialize();
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    do {
      EnumSet<IdleCondition> condChecks = EnumSet.noneOf(IdleCondition.class);
      if (!asyncTaskMonitor.isIdleNow()) {
        asyncTaskMonitor.notifyWhenIdle(new SignalingTask<Void>(NO_OP,
            IdleCondition.ASYNC_TASKS_HAVE_IDLED, generation));

        condChecks.add(IdleCondition.ASYNC_TASKS_HAVE_IDLED);
      }

      if (!compatIdle()) {
        compatTaskMonitor.get().notifyWhenIdle(new SignalingTask<Void>(NO_OP,
            IdleCondition.COMPAT_TASKS_HAVE_IDLED, generation));
        condChecks.add(IdleCondition.COMPAT_TASKS_HAVE_IDLED);
      }

      if (!idlingResourceRegistry.allResourcesAreIdle()) {
        final IdlingPolicy warning = IdlingPolicies.getDynamicIdlingResourceWarningPolicy();
        final IdlingPolicy error = IdlingPolicies.getDynamicIdlingResourceErrorPolicy();
        final SignalingTask<Void> idleSignal = new SignalingTask<Void>(NO_OP,
            IdleCondition.DYNAMIC_TASKS_HAVE_IDLED, generation);
        idlingResourceRegistry.notifyWhenAllResourcesAreIdle(new IdleNotificationCallback() {
          @Override
          public void resourcesStillBusyWarning(List<String> busyResourceNames) {
            warning.handleTimeout(busyResourceNames, "IdlingResources are still busy!");
          }

          @Override
          public void resourcesHaveTimedOut(List<String> busyResourceNames) {
            error.handleTimeout(busyResourceNames, "IdlingResources have timed out!");
            controllerHandler.post(idleSignal);
          }

          @Override
          public void allResourcesIdle() {
            controllerHandler.post(idleSignal);
          }
        });
        condChecks.add(IdleCondition.DYNAMIC_TASKS_HAVE_IDLED);
      }

      try {
        loopUntil(condChecks);
      } finally {
        asyncTaskMonitor.cancelIdleMonitor();
        if (compatTaskMonitor.isPresent()) {
          compatTaskMonitor.get().cancelIdleMonitor();
        }
        idlingResourceRegistry.cancelIdleMonitor();
      }
    } while (!asyncTaskMonitor.isIdleNow() || !compatIdle()
        || !idlingResourceRegistry.allResourcesAreIdle());

  }

  private boolean compatIdle() {
    if (compatTaskMonitor.isPresent()) {
      return compatTaskMonitor.get().isIdleNow();
    } else {
      return true;
    }
  }

  @Override
  public void loopMainThreadForAtLeast(long millisDelay) {
    initialize();
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    checkState(!IdleCondition.DELAY_HAS_PAST.isSignaled(conditionSet), "recursion detected!");

    checkArgument(millisDelay > 0);
    controllerHandler.postDelayed(new SignalingTask(NO_OP, IdleCondition.DELAY_HAS_PAST,
          generation),
        millisDelay);
    loopUntil(IdleCondition.DELAY_HAS_PAST);
    loopMainThreadUntilIdle();
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (!IdleCondition.handleMessage(msg, conditionSet, generation)) {
      Log.i(TAG, "Unknown message type: " + msg);
      return false;
    } else {
      return true;
    }
  }

  private void loopUntil(IdleCondition condition) {
    loopUntil(EnumSet.of(condition));
  }

  /**
   * Loops the main thread until all IdleConditions have been signaled.
   *
   * Once they've been signaled, the conditions are reset and the generation value
   * is incremented.
   *
   * Signals should only be raised thru SignalingTask instances, and care should be
   * taken to ensure that the signaling task is created before loopUntil is called.
   *
   * Good:
   * idlingType.runOnIdle(new SignalingTask(NO_OP, IdleCondition.MY_IDLE_CONDITION, generation));
   * loopUntil(IdleCondition.MY_IDLE_CONDITION);
   *
   * Bad:
   * idlingType.runOnIdle(new CustomCallback() {
   *   @Override
   *   public void itsDone() {
   *     // oh no - The creation of this signaling task is delayed until this method is
   *     // called, so it will not have the right value for generation.
   *     new SignalingTask(NO_OP, IdleCondition.MY_IDLE_CONDITION, generation).run();
   *  }
   * })
   * loopUntil(IdleCondition.MY_IDLE_CONDITION);
   */
  private void loopUntil(EnumSet<IdleCondition> conditions) {
    checkState(!looping, "Recursive looping detected!");
    looping = true;
    IdlingPolicy masterIdlePolicy = IdlingPolicies.getMasterIdlingPolicy();
    try {
      int loopCount = 0;
      long start = SystemClock.uptimeMillis();
      long end = start + masterIdlePolicy.getIdleTimeoutUnit().toMillis(
          masterIdlePolicy.getIdleTimeout());
      while (SystemClock.uptimeMillis() < end) {
        boolean conditionsMet = true;
        boolean shouldLogConditionState = loopCount > 0 && loopCount % 100 == 0;

        for (IdleCondition condition : conditions) {
          if (!condition.isSignaled(conditionSet)) {
            conditionsMet = false;
            if (shouldLogConditionState) {
              Log.w(TAG, "Waiting for: " + condition.name() + " for " + loopCount + " iterations.");
            } else {
              break;
            }
          }
        }

        if (conditionsMet) {
          QueueState queueState = queueInterrogator.determineQueueState();
          if (queueState == QueueState.EMPTY || queueState == QueueState.TASK_DUE_LONG) {
            return;
          } else {
            Log.v(
                "ESP_TRACE",

                "Barrier detected or task avaliable for running shortly.");
          }
        }

        Message message = queueInterrogator.getNextMessage();
        String callbackString = "unknown";
        String messageString = "unknown";
        try {
          if (null == message.getCallback()) {
            callbackString = "no callback.";
          } else {
            callbackString = message.getCallback().toString();
          }
          messageString = message.toString();
        } catch (NullPointerException e) {
          /*
           * Ignore. android.app.ActivityThread$ActivityClientRecord#toString() fails for API level
           * 15.
           */
        }

        Log.v(
            "ESP_TRACE",
            String.format("%s: MessageQueue.next(): %s, with target: %s, callback: %s", TAG,
              messageString, message.getTarget().getClass().getCanonicalName(), callbackString));
        message.getTarget().dispatchMessage(message);
        message.recycle();
        loopCount++;
      }
      List<String> idleConditions = Lists.newArrayList();
      for (IdleCondition condition : conditions) {
        if (!condition.isSignaled(conditionSet)) {
          idleConditions.add(condition.name());
        }
      }
      masterIdlePolicy.handleTimeout(idleConditions, String.format(
          "Looped for %s iterations over %s %s.", loopCount, masterIdlePolicy.getIdleTimeout(),
          masterIdlePolicy.getIdleTimeoutUnit().name()));
    } finally {
      looping = false;
      generation++;
      for (IdleCondition condition : conditions) {
        condition.reset(conditionSet);
      }
    }
  }


  private void initialize() {
    if (controllerHandler == null) {
      controllerHandler = new Handler(this);
    }
  }


  /**
   * Encapsulates posting a signal message to update the conditions set after a task has
   * executed.
   */
  private class SignalingTask<T> extends FutureTask<T> {

    private final IdleCondition condition;
    private final int myGeneration;

    public SignalingTask(Callable<T> callable, IdleCondition condition, int myGeneration) {
      super(callable);
      this.condition = checkNotNull(condition);
      this.myGeneration = myGeneration;
    }

    @Override
    protected void done() {
      controllerHandler.sendMessage(condition.createSignal(controllerHandler, myGeneration));
    }

  }

}
