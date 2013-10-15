package com.google.android.apps.common.testing.ui.testapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Custom edit text widget.
 */
public class DelegatingEditText extends LinearLayout {

  private final EditText delegateEditText;
  private final TextView messageView;
  private final Context mContext;

  public DelegatingEditText(Context context) {
    this(context, null);
  }

  public DelegatingEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOrientation(VERTICAL);
    mContext = context;
    LayoutInflater inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.delegating_edit_text, this, /* attachToRoot */ true);
    messageView = (TextView) findViewById(R.id.edit_text_message);
    delegateEditText = (EditText) findViewById(R.id.delegate_edit_text);
    delegateEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionCode, KeyEvent event) {
        messageView.setText("typed: " + delegateEditText.getText());
        messageView.setVisibility(View.VISIBLE);
        InputMethodManager imm =
            (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(delegateEditText.getWindowToken(), 0);
        return true;
      }
    });
  }
}
