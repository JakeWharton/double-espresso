package com.google.android.apps.common.testing.ui.espresso.base;

import com.google.android.apps.common.testing.ui.espresso.IdlingResource;
import com.google.android.apps.common.testing.ui.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;

import android.os.Handler;
import android.os.Looper;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link IdlingResourceRegistry}.
 */
public class IdlingResourceRegistryTest extends InstrumentationTestCase {

  private IdlingResourceRegistry registry;
  private Handler handler;

  @Override
  public void setUp() throws Exception {
    Looper looper = Looper.getMainLooper();
    handler = new Handler(looper);
    registry = new IdlingResourceRegistry(looper);
  }

  public void testRegisterDuplicates() {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    IdlingResource r1dup = new OnDemandIdlingResource("r1");
    registry.register(r1);
    registry.register(r1);
    registry.register(r1dup);
  }

  public void testAllResourcesAreIdle() throws InterruptedException {
    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    IdlingResource r3 = new OnDemandIdlingResource("r3");
    r1.forceIdleNow();
    r2.forceIdleNow();
    registry.register(r1);
    registry.register(r2);
    final AtomicBoolean resourcesIdle = new AtomicBoolean(false);
    final CountDownLatch latch = new CountDownLatch(1);
    handler.post(new Runnable() {
      @Override
      public void run() {
        resourcesIdle.set(registry.allResourcesAreIdle());
        latch.countDown();
      }
    });
    latch.await();
    assertTrue(resourcesIdle.get());

    final CountDownLatch latch2 = new CountDownLatch(1);
    registry.register(r3);
    handler.post(new Runnable() {
      @Override
      public void run() {
        resourcesIdle.set(registry.allResourcesAreIdle());
        latch2.countDown();
      }
    });
    latch2.await();
    assertFalse(resourcesIdle.get());
  }

  @LargeTest
  public void testAllResourcesAreIdle_RepeatingToIdleTransitions() throws InterruptedException {
    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    registry.register(r1);
    final AtomicBoolean resourcesIdle = new AtomicBoolean(false);
    for (int i = 1; i <= 3; i++) {
      final CountDownLatch latch = new CountDownLatch(1);
      handler.post(new Runnable() {
        @Override
        public void run() {
          resourcesIdle.set(registry.allResourcesAreIdle());
          latch.countDown();
        }
      });
      latch.await();
      assertFalse("Busy test " + i, resourcesIdle.get());

      r1.forceIdleNow();
      final CountDownLatch latch2 = new CountDownLatch(1);
      handler.post(new Runnable() {
        @Override
        public void run() {
          resourcesIdle.set(registry.allResourcesAreIdle());
          latch2.countDown();
        }
      });
      latch2.await();
      assertTrue("Idle transition test " + i, resourcesIdle.get());

      r1.reset();
    }
  }

  @LargeTest
  public void testNotifyWhenAllResourcesAreIdle_success() throws InterruptedException {
    final CountDownLatch busyWarningLatch = new CountDownLatch(4);
    final CountDownLatch timeoutLatch = new CountDownLatch(1);
    final CountDownLatch allResourcesIdleLatch = new CountDownLatch(1);
    final AtomicReference<List<String>> busysFromWarning = new AtomicReference<List<String>>();

    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    OnDemandIdlingResource r3 = new OnDemandIdlingResource("r3");
    registry.register(r1);
    registry.register(r2);
    registry.register(r3);

    handler.post(new Runnable() {

      @Override
      public void run() {
        registry.notifyWhenAllResourcesAreIdle(new IdleNotificationCallback() {
          private static final String TAG = "IdleNotificationCallback";
          @Override
          public void resourcesStillBusyWarning(List<String> busyResourceNames) {
            Log.w(TAG, "Timeout warning: " + busyResourceNames);
            busysFromWarning.set(busyResourceNames);
            busyWarningLatch.countDown();
          }

          @Override
          public void resourcesHaveTimedOut(List<String> busyResourceNames) {
            Log.w(TAG, "Timeout error: " + busyResourceNames);
            timeoutLatch.countDown();
          }

          @Override
          public void allResourcesIdle() {
            allResourcesIdleLatch.countDown();
          }
        });
      }
    });

    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(3, busysFromWarning.get().size());

    r3.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(2, busysFromWarning.get().size());

    r2.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(1, busysFromWarning.get().size());

    r1.forceIdleNow();
    assertTrue(allResourcesIdleLatch.await(200, TimeUnit.MILLISECONDS));
    assertEquals(1, busyWarningLatch.getCount());
    assertEquals(1, timeoutLatch.getCount());
  }

  @LargeTest
  public void testNotifyWhenAllResourcesAreIdle_timeout() throws InterruptedException {
    final CountDownLatch busyWarningLatch = new CountDownLatch(5);
    final CountDownLatch timeoutLatch = new CountDownLatch(1);
    final CountDownLatch allResourcesIdleLatch = new CountDownLatch(1);
    final AtomicReference<List<String>> busysFromWarning = new AtomicReference<List<String>>();

    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    OnDemandIdlingResource r3 = new OnDemandIdlingResource("r3");
    registry.register(r1);
    registry.register(r2);
    registry.register(r3);

    handler.post(new Runnable() {
      @Override
      public void run() {
        registry.notifyWhenAllResourcesAreIdle(new IdleNotificationCallback() {
          private static final String TAG = "IdleNotificationCallback";
          @Override
          public void resourcesStillBusyWarning(List<String> busyResourceNames) {
            Log.w(TAG, "Timeout warning: " + busyResourceNames);
            busysFromWarning.set(busyResourceNames);
            busyWarningLatch.countDown();
          }

          @Override
          public void resourcesHaveTimedOut(List<String> busyResourceNames) {
            Log.w(TAG, "Timeout error: " + busyResourceNames);
            timeoutLatch.countDown();
          }

          @Override
          public void allResourcesIdle() {
            allResourcesIdleLatch.countDown();
          }
        });
      }
    });

    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(3, busysFromWarning.get().size());

    r1.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(2, busysFromWarning.get().size());

    r2.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(1, busysFromWarning.get().size());

    assertTrue("Expected to finish count down", busyWarningLatch.await(8, TimeUnit.SECONDS));
    assertTrue("Should have timed out", timeoutLatch.await(2, TimeUnit.SECONDS));
    assertEquals(1, busysFromWarning.get().size());
    assertEquals(1, allResourcesIdleLatch.getCount());
  }
}
