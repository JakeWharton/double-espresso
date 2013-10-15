package com.google.android.apps.common.testing.ui.espresso.base;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;
import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.app.Activity;
import android.os.Build;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link EventInjector}.
 */
public class EventInjectorTest extends ActivityInstrumentationTestCase2<SendActivity> {
  private static final String TAG = EventInjectorTest.class.getSimpleName();
  private Activity sendActivity;
  private EventInjector injector;
  final AtomicBoolean injectEventWorked = new AtomicBoolean(false);
  final AtomicBoolean injectEventThrewSecurityException = new AtomicBoolean(false);
  final CountDownLatch latch = new CountDownLatch(1);

  @SuppressWarnings("deprecation")
  public EventInjectorTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", SendActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (Build.VERSION.SDK_INT > 15) {
      InputManagerEventInjectionStrategy strat = new InputManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    } else {
      WindowManagerEventInjectionStrategy strat = new WindowManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    }
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @LargeTest
  public void testInjectKeyEventUpWithNoDown() throws Exception {
    sendActivity = getActivity();

    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        View view = sendActivity.findViewById(R.id.send_data_edit_text);
        assertTrue(view.requestFocus());
        latch.countDown();
      }
    });

    assertTrue("Timed out!", latch.await(10, TimeUnit.SECONDS));
    KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
    KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
    assertTrue(injector.injectKeyEvent(events[1]));
  }

  @LargeTest
  public void testInjectStaleKeyEvent() throws Exception {
    sendActivity = getActivity();

    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        View view = sendActivity.findViewById(R.id.send_data_edit_text);
        assertTrue(view.requestFocus());
        latch.countDown();
      }
    });

    assertTrue("Timed out!", latch.await(10, TimeUnit.SECONDS));
    assertFalse("SecurityException exception was thrown.", injectEventThrewSecurityException.get());

    KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
    KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
    KeyEvent event = KeyEvent.changeTimeRepeat(events[0], 1, 0);

    // Stale event does not fail for API < 13.
    if (Build.VERSION.SDK_INT < 13) {
      assertTrue(injector.injectKeyEvent(event));
    } else {
      assertFalse(injector.injectKeyEvent(event));
    }
  }

  @LargeTest
  public void testInjectKeyEvent_securityException() {
    KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
    KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
    try {
      injector.injectKeyEvent(events[0]);
      fail("Should have thrown a security exception!");
    } catch (InjectEventSecurityException expected) { }
  }

  @LargeTest
  public void testInjectMotionEvent_securityException() throws Exception {
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        MotionEvent down = MotionEvent.obtain(SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_DOWN,
            0,
            0,
            0);
        try {
          injector.injectMotionEvent(down);
        } catch (InjectEventSecurityException expected) {
          injectEventThrewSecurityException.set(true);
        }
        latch.countDown();
      }
    });

    latch.await(10, TimeUnit.SECONDS);
    assertTrue(injectEventThrewSecurityException.get());
  }

  @LargeTest
  public void testInjectMotionEvent_upEventFailure() throws InterruptedException {
    final CountDownLatch activityStarted = new CountDownLatch(1);
    ActivityLifecycleCallback callback = new ActivityLifecycleCallback() {
      @Override
      public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        if (Stage.RESUMED == stage && activity instanceof SendActivity) {
          activityStarted.countDown();
        }
      }
    };
    ActivityLifecycleMonitorRegistry
        .getInstance()
        .addLifecycleCallback(callback);
    try {
      getActivity();
      assertTrue(activityStarted.await(20, TimeUnit.SECONDS));
      final int[] xy = UiControllerImplIntegrationTest.getCoordinatesInMiddleOfSendButton(
          getActivity(), getInstrumentation());

      getInstrumentation().runOnMainSync(new Runnable() {
        @Override
        public void run() {
          MotionEvent up = MotionEvent.obtain(SystemClock.uptimeMillis(),
              SystemClock.uptimeMillis(),
              MotionEvent.ACTION_UP,
              xy[0],
              xy[1],
              0);

          try {
            injectEventWorked.set(injector.injectMotionEvent(up));
          } catch (InjectEventSecurityException e) {
            Log.e(TAG, "injectEvent threw a SecurityException");
          }
          up.recycle();
          latch.countDown();
        }
      });

      latch.await(10, TimeUnit.SECONDS);
      assertFalse(injectEventWorked.get());
    } finally {
      ActivityLifecycleMonitorRegistry
          .getInstance()
          .removeLifecycleCallback(callback);
    }

  }
}
