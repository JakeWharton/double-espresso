package com.google.android.apps.common.testing.ui.espresso.base;

import com.google.android.apps.common.testing.ui.espresso.IdlingResourceTimeoutException;
import com.google.common.base.Optional;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test for {@link UiControllerImpl}.
 */
public class UiControllerImplTest extends TestCase {

  private static final String TAG = UiControllerImplTest.class.getSimpleName();

  private LooperThread testThread;
  private AtomicReference<UiControllerImpl> uiController = new AtomicReference<UiControllerImpl>();
  private ThreadPoolExecutor asyncPool;
  private IdlingResourceRegistry idlingResourceRegistry;

  private static class LooperThread extends Thread {
    private final CountDownLatch init = new CountDownLatch(1);
    private Handler handler;
    private Looper looper;

    @Override
    public void run() {
      Looper.prepare();
      handler = new Handler();
      looper = Looper.myLooper();
      init.countDown();
      Looper.loop();
    }

    public void quitLooper() {
      looper.quit();
    }

    public Looper getLooper() {
      try {
        init.await();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return looper;
    }

    public Handler getHandler() {
      try {
        init.await();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return handler;
    }
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    testThread = new LooperThread();
    testThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "Looper died: ", ex);
      }
    });
    testThread.start();
    idlingResourceRegistry = new IdlingResourceRegistry(testThread.getLooper());
    asyncPool = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>());
    EventInjector injector = null;
    if (Build.VERSION.SDK_INT > 15) {
      InputManagerEventInjectionStrategy strat = new InputManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    } else {
      WindowManagerEventInjectionStrategy strat = new WindowManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    }
    uiController.set(new UiControllerImpl(
        injector,
        new AsyncTaskPoolMonitor(asyncPool),
        Optional.<AsyncTaskPoolMonitor>absent(),
        idlingResourceRegistry,
        testThread.getLooper()
        ));


  }

  @Override
  public void tearDown() throws Exception {
    testThread.quitLooper();
    asyncPool.shutdown();
    super.tearDown();
  }

  public void testLoopMainThreadTillIdle_sendsMessageToRightHandler() {
    final CountDownLatch latch = new CountDownLatch(3);
    testThread.getHandler(); // blocks till initialized;
    final Handler firstHandler = new Handler(
        testThread.looper,
        new Handler.Callback() {
          private boolean counted = false;
          @Override
          public boolean handleMessage(Message me) {
            if (counted) {
              fail("Called 2x!!!!");
            }
            counted = true;
            latch.countDown();
            return true;
          }
        });

    final Handler secondHandler = new Handler(
        testThread.looper,
        new Handler.Callback() {
          private boolean counted = false;
          @Override
          public boolean handleMessage(Message me) {
            if (counted) {
              fail("Called 2x!!!!");
            }
            counted = true;
            latch.countDown();
            return true;
          }
        });

    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        firstHandler.sendEmptyMessage(1);
        secondHandler.sendEmptyMessage(1);
        uiController.get().loopMainThreadUntilIdle();

        latch.countDown();
      }
    }));

    try {
      assertTrue(
          "Timed out waiting for looper to process all events", latch.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail("Failed with exception " + e);
    }
  }

  public void testLoopForAtLeast() throws Exception {
    final CountDownLatch latch = new CountDownLatch(2);
    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        testThread.getHandler().post(new Runnable() {
          @Override
          public void run() {
            latch.countDown();
          }

        });
        uiController.get().loopMainThreadForAtLeast(1000);
        latch.countDown();
      }
    }));
    assertTrue("Never returned from UiControllerImpl.loopMainThreadForAtLeast();",
        latch.await(10, TimeUnit.SECONDS));
  }

  public void testLoopMainThreadUntilIdle_fullQueue() {
    final CountDownLatch latch = new CountDownLatch(3);
    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "On main thread");
        Handler handler = new Handler();
        Log.i(TAG, "Equeueing test runnable 1");
        handler.post(new Runnable() {
          @Override
          public void run() {
            Log.i(TAG, "Running test runnable 1");
            latch.countDown();
          }
        });
        Log.i(TAG, "Equeueing test runnable 2");
        handler.post(new Runnable() {
          @Override
          public void run() {
            Log.i(TAG, "Running test runnable 2");
            latch.countDown();
          }
        });
        Log.i(TAG, "Hijacking thread and looping it.");
        uiController.get().loopMainThreadUntilIdle();
        latch.countDown();
      }
    }));

    try {
      assertTrue(
          "Timed out waiting for looper to process all events", latch.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail("Failed with exception " + e);
    }
  }

  public void testLoopMainThreadUntilIdle_fullQueueAndAsyncTasks() throws Exception {
    final CountDownLatch latch = new CountDownLatch(3);
    final CountDownLatch asyncTaskStarted = new CountDownLatch(1);
    final CountDownLatch asyncTaskShouldComplete = new CountDownLatch(1);
    asyncPool.execute(new Runnable() {
      @Override
      public void run() {
        asyncTaskStarted.countDown();
        while (true) {
          try {
            asyncTaskShouldComplete.await();
            return;
          } catch (InterruptedException ie) {
            // cant interrupt me. ignore.
          }
        }
      }
    });
    assertTrue("async task is not starting!", asyncTaskStarted.await(2, TimeUnit.SECONDS));

    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "On main thread");
        Handler handler = new Handler();
        Log.i(TAG, "Equeueing test runnable 1");
        handler.post(new Runnable() {
          @Override
          public void run() {
            Log.i(TAG, "Running test runnable 1");
            latch.countDown();
          }
        });
        Log.i(TAG, "Equeueing test runnable 2");
        handler.post(new Runnable() {
          @Override
          public void run() {
            Log.i(TAG, "Running test runnable 2");
            latch.countDown();
          }
        });
        Log.i(TAG, "Hijacking thread and looping it.");
        uiController.get().loopMainThreadUntilIdle();
        latch.countDown();
      }
    }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(2, TimeUnit.SECONDS));
    assertEquals("Not all main thread tasks have checked in", 1L, latch.getCount());
    asyncTaskShouldComplete.countDown();
    assertTrue("App should be idle.", latch.await(5, TimeUnit.SECONDS));
  }


  public void testLoopMainThreadUntilIdle_emptyQueue() {
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        uiController.get().loopMainThreadUntilIdle();
        latch.countDown();
      }
    }));
    try {
      assertTrue("Never returned from UiControllerImpl.loopMainThreadUntilIdle();",
          latch.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail("Failed with exception " + e);
    }
  }

  public void testLoopMainThreadUntilIdle_oneIdlingResource() throws InterruptedException {
    OnDemandIdlingResource fakeResource = new OnDemandIdlingResource("FakeResource");
    idlingResourceRegistry.register(fakeResource);
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "Hijacking thread and looping it.");
        uiController.get().loopMainThreadUntilIdle();
        latch.countDown();
      }
    }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(2, TimeUnit.SECONDS));
    fakeResource.forceIdleNow();
    assertTrue("App should be idle.", latch.await(5, TimeUnit.SECONDS));
  }

  public void testLoopMainThreadUntilIdle_multipleIdlingResources() throws InterruptedException {
    OnDemandIdlingResource fakeResource1 = new OnDemandIdlingResource("FakeResource1");
    OnDemandIdlingResource fakeResource2 = new OnDemandIdlingResource("FakeResource2");
    OnDemandIdlingResource fakeResource3 = new OnDemandIdlingResource("FakeResource3");
    // Register the first two right away and one later (once the wait for the first two begins).
    idlingResourceRegistry.register(fakeResource1);
    idlingResourceRegistry.register(fakeResource2);
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "Hijacking thread and looping it.");
        uiController.get().loopMainThreadUntilIdle();
        latch.countDown();
      }
    }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    fakeResource1.forceIdleNow();
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    idlingResourceRegistry.register(fakeResource3);
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    fakeResource2.forceIdleNow();
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    fakeResource3.forceIdleNow();
    assertTrue("App should be idle.", latch.await(5, TimeUnit.SECONDS));
  }

  @LargeTest
  public void testLoopMainThreadUntilIdle_timeout() throws InterruptedException {
    OnDemandIdlingResource goodResource =
        new OnDemandIdlingResource("GoodResource");
    OnDemandIdlingResource kindaCrappyResource =
        new OnDemandIdlingResource("KindaCrappyResource");
    OnDemandIdlingResource badResource =
        new OnDemandIdlingResource("VeryBadResource");
    idlingResourceRegistry.register(goodResource);
    idlingResourceRegistry.register(kindaCrappyResource);
    idlingResourceRegistry.register(badResource);
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(testThread.getHandler().post(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "Hijacking thread and looping it.");
        try {
          uiController.get().loopMainThreadUntilIdle();
        } catch (IdlingResourceTimeoutException e) {
          latch.countDown();
        }
      }
    }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(4, TimeUnit.SECONDS));
    goodResource.forceIdleNow();
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(12, TimeUnit.SECONDS));
    kindaCrappyResource.forceIdleNow();
    assertTrue(
        "Should have caught IdlingResourceTimeoutException", latch.await(11, TimeUnit.SECONDS));
  }

}
