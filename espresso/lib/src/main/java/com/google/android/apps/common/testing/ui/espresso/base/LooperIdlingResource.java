package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.android.apps.common.testing.ui.espresso.IdlingResource;
import com.google.android.apps.common.testing.ui.espresso.IdlingResource.ResourceCallback;
import com.google.android.apps.common.testing.ui.espresso.base.QueueInterrogator.QueueState;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;

/**
 * An Idling Resource Adapter for Loopers.
 */
final class LooperIdlingResource implements IdlingResource {

  private static final String TAG = "LooperIdleResource";

  private final boolean considerWaitIdle;
  private final Looper monitoredLooper;
  private final Handler monitoredHandler;

  private ResourceCallback resourceCallback;

  LooperIdlingResource(Looper monitoredLooper, boolean considerWaitIdle) {
    this.monitoredLooper = checkNotNull(monitoredLooper);
    this.monitoredHandler = new Handler(monitoredLooper);
    this.considerWaitIdle = considerWaitIdle;
    checkState(Looper.getMainLooper() != monitoredLooper, "Not for use with main looper.");
  }

  // Only assigned and read from the main loop.
  private QueueInterrogator queueInterrogator;

  @Override
  public String getName() {
    return monitoredLooper.getThread().getName();
  }

  @Override
  public boolean isIdleNow() {
    // on main thread here.
    QueueState state = queueInterrogator.determineQueueState();
    boolean idle = state == QueueState.EMPTY || state == QueueState.TASK_DUE_LONG;
    boolean idleWait = considerWaitIdle
        && monitoredLooper.getThread().getState() == Thread.State.WAITING;
    if (idleWait) {
      if (resourceCallback != null) {
        resourceCallback.onTransitionToIdle();
      }
    }
    return idle || idleWait;
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
    this.resourceCallback = resourceCallback;
    // on main thread here.
    queueInterrogator = new QueueInterrogator(monitoredLooper);

    // must load idle handlers from monitored looper thread.
    IdleHandler idleHandler = new ResourceCallbackIdleHandler(resourceCallback, queueInterrogator,
        monitoredHandler);

    checkState(monitoredHandler.postAtFrontOfQueue(new Initializer(idleHandler)),
          "Monitored looper exiting.");
  }

  private static class ResourceCallbackIdleHandler implements IdleHandler {
    private final ResourceCallback resourceCallback;
    private final QueueInterrogator myInterrogator;
    private final Handler myHandler;

    ResourceCallbackIdleHandler(ResourceCallback resourceCallback,
        QueueInterrogator myInterrogator, Handler myHandler) {
      this.resourceCallback = checkNotNull(resourceCallback);
      this.myInterrogator = checkNotNull(myInterrogator);
      this.myHandler = checkNotNull(myHandler);
    }

    @Override
    public boolean queueIdle() {
      // invoked on the monitored looper thread.
      QueueState queueState = myInterrogator.determineQueueState();
      if (queueState == QueueState.EMPTY || queueState == QueueState.TASK_DUE_LONG) {
        // no block and no task coming 'shortly'.
        resourceCallback.onTransitionToIdle();
      } else if (queueState == QueueState.BARRIER) {
        // send a sentinal message that'll cause us to queueIdle again once the
        // block is lifted.
        myHandler.sendEmptyMessage(-1);
      }

      return true;
    }
  }

  private static class Initializer implements Runnable {
    private final IdleHandler myIdleHandler;

    Initializer(IdleHandler myIdleHandler) {
      this.myIdleHandler = checkNotNull(myIdleHandler);
    }

    @Override
    public void run() {
      // on monitored looper thread.
      Looper.myQueue().addIdleHandler(myIdleHandler);
    }
  }

}
