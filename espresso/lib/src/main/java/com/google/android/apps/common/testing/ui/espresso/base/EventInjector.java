package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Responsible for selecting the proper strategy for injecting MotionEvents to the application under
 * test.
 */
final class EventInjector {
  private static final String TAG = EventInjector.class.getSimpleName();
  private final EventInjectionStrategy injectionStrategy;

  EventInjector(EventInjectionStrategy injectionStrategy) {
    this.injectionStrategy = checkNotNull(injectionStrategy);
  }

  boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
    long downTime = event.getDownTime();
    long eventTime = event.getEventTime();
    int action = event.getAction();
    int code = event.getKeyCode();
    int repeatCount = event.getRepeatCount();
    int metaState = event.getMetaState();
    int deviceId = event.getDeviceId();
    int scancode = event.getScanCode();
    int flags = event.getFlags();

    if (eventTime == 0) {
      eventTime = SystemClock.uptimeMillis();
    }

    if (downTime == 0) {
      downTime = eventTime;
    }

    // API < 9 does not have constructor with source (nor has source field).
    KeyEvent newEvent;
    if (Build.VERSION.SDK_INT < 9) {
      newEvent = new KeyEvent(downTime,
          eventTime,
          action,
          code,
          repeatCount,
          metaState,
          deviceId,
          scancode,
          flags | KeyEvent.FLAG_FROM_SYSTEM);
    } else {
      int source = event.getSource();
      newEvent = new KeyEvent(downTime,
          eventTime,
          action,
          code,
          repeatCount,
          metaState,
          deviceId,
          scancode,
          flags | KeyEvent.FLAG_FROM_SYSTEM,
          source);
    }

    Log.v(
        "ESP_TRACE",
        String.format(
            "%s:Injecting event for character (%c) with key code (%s) downtime: (%s)", TAG,
            newEvent.getUnicodeChar(), newEvent.getKeyCode(), newEvent.getDownTime()));

    return injectionStrategy.injectKeyEvent(newEvent);
  }

  boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
    return injectionStrategy.injectMotionEvent(event);
  }

}
