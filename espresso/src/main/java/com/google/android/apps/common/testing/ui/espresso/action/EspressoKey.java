package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.common.base.Preconditions.checkState;

import android.os.Build;
import android.view.KeyEvent;

/**
 * Class that wraps the key code and meta state of the desired key press.
 */
public final class EspressoKey {
  private final int keyCode;
  private final int metaState;

  private EspressoKey(Builder builder) {
    this.keyCode = builder.builderKeyCode;
    this.metaState = builder.getMetaState();
  }

  public int getKeyCode() {
    return keyCode;
  }

  public int getMetaState() {
    return metaState;
  }

  @Override
  public String toString() {
    return String.format("keyCode: %s, metaState: %s", keyCode, metaState);
  }

  /**
   * Builder for the EspressoKey class.
   */
  public static class Builder {
    private int builderKeyCode = -1;
    private boolean isShiftPressed;
    private boolean isAltPressed;
    private boolean isCtrlPressed;

    public Builder withKeyCode(int keyCode) {
      builderKeyCode = keyCode;
      return this;
    }

    /**
     * Sets the SHIFT_ON meta state of the resulting key.
     */
    public Builder withShiftPressed(boolean shiftPressed) {
      isShiftPressed = shiftPressed;
      return this;
    }

    /**
     * On Honeycomb and above, sets the CTRL_ON meta state of the resulting key. On Gingerbread and
     * below, this is a noop.
     */
    public Builder withCtrlPressed(boolean ctrlPressed) {
      isCtrlPressed = ctrlPressed;
      return this;
    }

    /**
     * Sets the ALT_ON meta state of the resulting key.
     */
    public Builder withAltPressed(boolean altPressed) {
      isAltPressed = altPressed;
      return this;
    }

    private int getMetaState() {
      int metaState = 0;
      if (isShiftPressed) {
        metaState |= KeyEvent.META_SHIFT_ON;
      }

      if (isAltPressed) {
        metaState |= KeyEvent.META_ALT_ON;
      }

      if (isCtrlPressed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        metaState |= KeyEvent.META_CTRL_ON;
      }

      return metaState;
    }

    public EspressoKey build() {
      checkState(builderKeyCode > 0 && builderKeyCode < KeyEvent.getMaxKeyCode(),
          "Invalid key code: %s", builderKeyCode);
      return new EspressoKey(this);
    }
  }
}
