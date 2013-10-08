package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.propagate;

import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;

import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An {@link EventInjectionStrategy} that uses the input manager to inject Events.
 * This strategy supports API level 16 and above.
 */
final class InputManagerEventInjectionStrategy implements EventInjectionStrategy {
  private static final String TAG = InputManagerEventInjectionStrategy.class.getSimpleName();

  // Used in reflection
  private boolean initComplete;
  private Method injectInputEventMethod;
  private Method setSourceMotionMethod;
  private Object instanceInputManagerObject;
  private int motionEventMode;
  private int keyEventMode;

  InputManagerEventInjectionStrategy() {
    checkState(Build.VERSION.SDK_INT >= 16, "Unsupported API level.");
  }

  void initialize() {
    if (initComplete) {
      return;
    }

    try {
      Log.d(TAG, "Creating injection strategy with input manager.");

      // Get the InputputManager class object and initialize if necessary.
      Class<?> inputManagerClassObject = Class.forName("android.hardware.input.InputManager");
      Method getInstanceMethod = inputManagerClassObject.getDeclaredMethod("getInstance");
      getInstanceMethod.setAccessible(true);

      instanceInputManagerObject = getInstanceMethod.invoke(inputManagerClassObject);

      injectInputEventMethod = instanceInputManagerObject.getClass()
          .getDeclaredMethod("injectInputEvent", InputEvent.class, Integer.TYPE);
      injectInputEventMethod.setAccessible(true);

      // Setting event mode to INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH to ensure
      // that we've dispatched the event and any side effects its had on the view hierarchy
      // have occurred.
      Field motionEventModeField =
          inputManagerClassObject.getField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");
      motionEventModeField.setAccessible(true);
      motionEventMode = motionEventModeField.getInt(inputManagerClassObject);

      Field keyEventModeField =
          inputManagerClassObject.getField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");
      keyEventModeField.setAccessible(true);
      keyEventMode = keyEventModeField.getInt(inputManagerClassObject);

      setSourceMotionMethod = MotionEvent.class.getDeclaredMethod("setSource", Integer.TYPE);
      InputEvent.class.getDeclaredMethod("getSequenceNumber");
      initComplete = true;
    } catch (ClassNotFoundException e) {
      propagate(e);
    } catch (IllegalAccessException e) {
      propagate(e);
    } catch (IllegalArgumentException e) {
      propagate(e);
    } catch (InvocationTargetException e) {
      propagate(e);
    } catch (NoSuchMethodException e) {
      propagate(e);
    } catch (SecurityException e) {
      propagate(e);
    } catch (NoSuchFieldException e) {
      propagate(e);
    }
  }

  @Override
  public boolean injectKeyEvent(KeyEvent keyEvent) throws InjectEventSecurityException {
    try {
       return (Boolean) injectInputEventMethod.invoke(instanceInputManagerObject,
           keyEvent, keyEventMode);
    } catch (IllegalAccessException e) {
      propagate(e);
    } catch (IllegalArgumentException e) {
      propagate(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SecurityException) {
        throw new InjectEventSecurityException(cause);
      }
      propagate(e);
    } catch (SecurityException e) {
      throw new InjectEventSecurityException(e);
    }
    return false;
  }

  @Override
  public boolean injectMotionEvent(MotionEvent motionEvent) throws InjectEventSecurityException {
    try {
      // Need to set the event source to touch screen, otherwise the input can be ignored even
      // though injecting it would be successful.
      // TODO(user): proper handling of events from a trackball (SOURCE_TRACKBALL) and joystick.
      if ((motionEvent.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0
          && !isFromTouchpadInGlassDevice(motionEvent)) {
        // Need to do runtime invocation of setSource because it was not added until 2.3_r1.
        setSourceMotionMethod.invoke(motionEvent, InputDevice.SOURCE_TOUCHSCREEN);
      }
      return (Boolean) injectInputEventMethod.invoke(instanceInputManagerObject,
          motionEvent, motionEventMode);
    } catch (IllegalAccessException e) {
      propagate(e);
    } catch (IllegalArgumentException e) {
      propagate(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SecurityException) {
        throw new InjectEventSecurityException(cause);
      }
      propagate(e);
    } catch (SecurityException e) {
      throw new InjectEventSecurityException(e);
    }
    return false;
  }

  // We'd like to inject non-pointer events sourced from touchpad in Glass.
  private static boolean isFromTouchpadInGlassDevice(MotionEvent motionEvent) {
    return (Build.DEVICE.contains("glass")
        || Build.DEVICE.contains("Glass") || Build.DEVICE.contains("wingman"))
        && ((motionEvent.getSource() & InputDevice.SOURCE_TOUCHPAD) != 0);
  }
}
