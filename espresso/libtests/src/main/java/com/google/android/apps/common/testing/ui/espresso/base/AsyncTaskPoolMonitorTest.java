package com.google.android.apps.common.testing.ui.espresso.base;

import junit.framework.TestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test for {@link AsyncTaskPoolMonitor}
 */
public class AsyncTaskPoolMonitorTest extends TestCase {

  private final ThreadPoolExecutor testThreadPool = new ThreadPoolExecutor(
      4, 4, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  private AsyncTaskPoolMonitor monitor = new AsyncTaskPoolMonitor(testThreadPool);

  @Override
  public void tearDown() throws Exception {
    testThreadPool.shutdownNow();
    super.tearDown();
  }

  public void testIsIdle_onEmptyPool() throws Exception {
    assertTrue(monitor.isIdleNow());
    final AtomicBoolean isIdle = new AtomicBoolean(false);
    // since we're already idle, this should be ran immedately on our thread.
    monitor.notifyWhenIdle(new Runnable() {
      @Override
      public void run() {
        isIdle.set(true);
      }
    });
    assertTrue(isIdle.get());
  }

  public void testIsIdle_withRunningTask() throws Exception {
    final CountDownLatch runLatch = new CountDownLatch(1);
    testThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        runLatch.countDown();
        try {
          Thread.sleep(50000);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    });
    assertTrue(runLatch.await(1, TimeUnit.SECONDS));
    assertFalse(monitor.isIdleNow());

    final AtomicBoolean isIdle = new AtomicBoolean(false);
    monitor.notifyWhenIdle(new Runnable() {
      @Override
      public void run() {
        isIdle.set(true);
      }
    });
    // runnable shouldn't be run ever..
    assertFalse(isIdle.get());
  }


  public void testIdleNotificationAndRestart() throws Exception {

    FutureTask<Thread> workerThreadFetchTask = new FutureTask<Thread>(new Callable<Thread>() {
      @Override
      public Thread call() {
        return Thread.currentThread();
      }
    });
    testThreadPool.submit(workerThreadFetchTask);

    Thread workerThread = workerThreadFetchTask.get();

    final CountDownLatch runLatch = new CountDownLatch(1);
    final CountDownLatch exitLatch = new CountDownLatch(1);

    testThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        runLatch.countDown();
        try {
          exitLatch.await();
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    });

    assertTrue(runLatch.await(1, TimeUnit.SECONDS));
    final CountDownLatch notificationLatch = new CountDownLatch(1);
    monitor.notifyWhenIdle(new Runnable() {
      @Override
      public void run() {
        notificationLatch.countDown();
      }
    });
    // give some time for the idle detection threads to spin up.
    Thread.sleep(2000);
    // interrupt one of them
    workerThread.interrupt();
    Thread.sleep(1000);
    // unblock the dummy work item.
    exitLatch.countDown();
    assertTrue(notificationLatch.await(1, TimeUnit.SECONDS));
    assertTrue(monitor.isIdleNow());
  }

  public void testIdleNotification_extraWork() throws Exception {
    final CountDownLatch firstRunLatch = new CountDownLatch(1);
    final CountDownLatch firstExitLatch = new CountDownLatch(1);

    testThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        firstRunLatch.countDown();
        try {
          firstExitLatch.await();
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    });

    assertTrue(firstRunLatch.await(1, TimeUnit.SECONDS));

    final CountDownLatch notificationLatch = new CountDownLatch(1);
    monitor.notifyWhenIdle(new Runnable() {
      @Override
      public void run() {
        notificationLatch.countDown();
      }
    });

    final CountDownLatch secondRunLatch = new CountDownLatch(1);
    final CountDownLatch secondExitLatch = new CountDownLatch(1);
    testThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        secondRunLatch.countDown();
        try {
          secondExitLatch.await();
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    });

    assertFalse(notificationLatch.await(10, TimeUnit.MILLISECONDS));
    firstExitLatch.countDown();
    assertFalse(notificationLatch.await(500, TimeUnit.MILLISECONDS));
    secondExitLatch.countDown();
    assertTrue(notificationLatch.await(1, TimeUnit.SECONDS));
    assertTrue(monitor.isIdleNow());
  }
}
