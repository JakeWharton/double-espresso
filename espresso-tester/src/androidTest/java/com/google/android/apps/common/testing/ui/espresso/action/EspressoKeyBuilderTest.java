package com.google.android.apps.common.testing.ui.espresso.action;

import com.google.android.apps.common.testing.ui.espresso.action.EspressoKey.Builder;

import android.os.Build;
import android.view.KeyEvent;

import junit.framework.TestCase;

/**
 * Unit tests for {@link Builder}.
 */
public class EspressoKeyBuilderTest extends TestCase {

  static final int KEY_CODE = KeyEvent.KEYCODE_X;

  public void testBuildWithNoMetaState() {
    EspressoKey key = new Builder().withKeyCode(KEY_CODE).build();
    assertEquals(KEY_CODE, key.getKeyCode());
    assertEquals(0, key.getMetaState());
  }

  public void testBuildWithShiftPressed() {
    EspressoKey key = new Builder().withKeyCode(KEY_CODE).withShiftPressed(true).build();
    assertEquals(KEY_CODE, key.getKeyCode());
    assertEquals(KeyEvent.META_SHIFT_ON, key.getMetaState());
  }

  public void testBuildWithCtrlPressed() {
    EspressoKey key = new Builder().withKeyCode(KEY_CODE).withCtrlPressed(true).build();
    assertEquals(KEY_CODE, key.getKeyCode());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      assertEquals(KeyEvent.META_CTRL_ON, key.getMetaState());
    } else {
      assertEquals(0, key.getMetaState());
    }
  }

  public void testBuildWithAltPressed() {
    EspressoKey key = new Builder().withKeyCode(KEY_CODE).withAltPressed(true).build();
    assertEquals(KEY_CODE, key.getKeyCode());
    assertEquals(KeyEvent.META_ALT_ON, key.getMetaState());
  }

  public void testBuildWithAllMetaKeysPressed() {
    EspressoKey key = new Builder().withKeyCode(KEY_CODE)
        .withShiftPressed(true)
        .withCtrlPressed(true)
        .withAltPressed(true)
        .build();

    assertEquals(KEY_CODE, key.getKeyCode());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      assertEquals(KeyEvent.META_SHIFT_ON | KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON,
          key.getMetaState());
    } else {
      assertEquals(KeyEvent.META_SHIFT_ON | KeyEvent.META_ALT_ON, key.getMetaState());
    }
  }
}