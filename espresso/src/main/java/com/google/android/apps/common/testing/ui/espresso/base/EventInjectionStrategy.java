package com.google.android.apps.common.testing.ui.espresso.base;

import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;

import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Injects Events into the application under test. Implementors should expect to be called
 * from the UI thread and are responsible for ensuring the event gets delivered or indicating that
 * it could not be delivered.
 */
interface EventInjectionStrategy {
  /**
   * Injects the given {@link KeyEvent} into the android system.
   *
   * @param keyEvent The event to inject
   * @return {@code true} if the input was inject successfully, {@code false} otherwise.
   * @throws InjectEventSecurityException if the MotionEvent would be delivered to an area of the
   *         screen that is not owned by the application under test.
   */
  boolean injectKeyEvent(KeyEvent keyEvent) throws InjectEventSecurityException;

  /**
   * Injects the given {@link MotionEvent} into the android system.
   *
   * @param motionEvent The event to inject
   * @return {@code true} if the input was inject successfully, {@code false} otherwise.
   * @throws InjectEventSecurityException if the MotionEvent would be delivered to an area of the
   *         screen that is not owned by the application under test.
   */
  boolean injectMotionEvent(MotionEvent motionEvent) throws InjectEventSecurityException;

}
