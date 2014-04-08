package com.google.android.apps.common.testing.ui.espresso.action;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.android.apps.common.testing.ui.espresso.InjectEventSecurityException;
import com.google.android.apps.common.testing.ui.espresso.PerformException;
import com.google.android.apps.common.testing.ui.espresso.UiController;

import android.view.MotionEvent;
import android.view.View;

import junit.framework.TestCase;

import org.mockito.Mock;

/**
 * Unit tests for {@link TypeTextAction}.
 */
public class TypeTextActionTest extends TestCase {
  @Mock
  private UiController mockUiController;

  @Mock
  private View mockView;

  private TypeTextAction typeTextAction;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    initMocks(this);
  }

  public void testTypeTextActionPerform() throws InjectEventSecurityException {
    String stringToBeTyped = "Hello!";
    typeTextAction = new TypeTextAction(stringToBeTyped);
    when(mockUiController.injectMotionEvent(isA(MotionEvent.class))).thenReturn(true);
    when(mockUiController.injectString(stringToBeTyped)).thenReturn(true);
    typeTextAction.perform(mockUiController, mockView);
  }

  public void testTypeTextActionPerformFailed() throws InjectEventSecurityException {
    String stringToBeTyped = "Hello!";
    typeTextAction = new TypeTextAction(stringToBeTyped);
    when(mockUiController.injectMotionEvent(isA(MotionEvent.class))).thenReturn(true);
    when(mockUiController.injectString(stringToBeTyped)).thenReturn(false);

    try {
      typeTextAction.perform(mockUiController, mockView);
      fail("Should have thrown PerformException");
    } catch (PerformException e) {
      if (e.getCause() instanceof InjectEventSecurityException) {
        fail("Exception cause should NOT be of type InjectEventSecurityException");
      }
    }
  }

  public void testTypeTextActionPerformInjectEventSecurityException()
      throws InjectEventSecurityException {
    String stringToBeTyped = "Hello!";
    typeTextAction = new TypeTextAction(stringToBeTyped);
    when(mockUiController.injectMotionEvent(isA(MotionEvent.class))).thenReturn(true);
    when(mockUiController.injectString(stringToBeTyped))
        .thenThrow(new InjectEventSecurityException(""));

    try {
      typeTextAction.perform(mockUiController, mockView);
      fail("Should have thrown PerformException");
    } catch (PerformException e) {
      if (!(e.getCause() instanceof InjectEventSecurityException)) {
        fail("Exception cause should be of type InjectEventSecurityException");
      }
    }
  }
}
