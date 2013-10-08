package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Simple activity used for validating intent sending and UI behavior.
 */
public class SendActivity extends Activity {

  private static final int PICK_CONTACT_REQUEST = 1;  // The request code
  static final String EXTRA_DATA = "com.google.android.apps.common.testing.ui.testapp.DATA";
  static final int PICK_CONTACT = 100;
  private PopupWindow popupWindow;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.send_activity);

    EditText editText = (EditText) findViewById(R.id.enterDataEditText);
    editText.setOnKeyListener(new OnKeyListener() {

      @Override
      public boolean onKey(View view, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
            (keyCode == KeyEvent.KEYCODE_ENTER)) {
          EditText editText = (EditText) view;
          TextView responseText = (TextView) findViewById(R.id.enterDataResponseText);
          responseText.setText(editText.getText());
          return true;
        } else {
          return false;
        }
      }
    });

    final EditText searchBox = (EditText) findViewById(R.id.searchBox);
    searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          TextView result = (TextView) findViewById(R.id.searchResult);
          result.setText(getString(R.string.searching_for_label) + " " + v.getText());
          result.setVisibility(View.VISIBLE);
          InputMethodManager imm =
              (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
          return true;
        }
        return false;
      }
    });
  }

  /** Called when user clicks the Send button */
  public void sendData(@SuppressWarnings("unused") View view) {
    Intent intent = new Intent(this, DisplayActivity.class);
    EditText editText = (EditText) findViewById(R.id.sendDataEditText);
    intent.putExtra(EXTRA_DATA, editText.getText().toString());
    startActivity(intent);
  }

  public void sendDataToCall(@SuppressWarnings("unused") View view) {
    Intent intentToCall = new Intent(Intent.ACTION_CALL);
    EditText editText = (EditText) findViewById(R.id.sendDataToCallEditText);
    String number = editText.getText().toString();
    intentToCall.setData(Uri.parse("tel:" + number));
    startActivity(intentToCall);
  }

  public void sendDataToBrowser(@SuppressWarnings("unused") View view) {
    EditText editText = (EditText) findViewById(R.id.sendDataToBrowserEditText);
    String url = editText.getText().toString();
    Intent intentToBrowser = new Intent(Intent.ACTION_VIEW);
    intentToBrowser.setData(Uri.parse(url));
    intentToBrowser.addCategory(Intent.CATEGORY_BROWSABLE);
    intentToBrowser.putExtra("key1", "value1");
    intentToBrowser.putExtra("key2", "value2");
    startActivity(intentToBrowser);
  }

  public void sendMessage(@SuppressWarnings("unused") View view) {
    Intent sendIntent = new Intent();
    EditText editText = (EditText) findViewById(R.id.sendDataToMessageEditText);
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
    sendIntent.setType("text/plain");
    startActivity(sendIntent);
  }

  public void clickToMarket(@SuppressWarnings("unused") View view) {
    Intent marketIntent = new Intent(Intent.ACTION_VIEW);
    EditText editText = (EditText) findViewById(R.id.sendToMarketData);
    marketIntent.setData(Uri.parse(
        "market://details?id=" + editText.getText().toString()));
    startActivity(marketIntent);
  }

  public void clickToGesture(@SuppressWarnings("unused") View view) {
    startActivity(new Intent(this, GestureActivity.class));
  }

  public void clickToScroll(@SuppressWarnings("unused") View view) {
    startActivity(new Intent(this, ScrollActivity.class));
  }

  public void clickToList(@SuppressWarnings("unused") View view) {
    startActivity(new Intent(this, LongListActivity.class));
  }

  public boolean showDialog(@SuppressWarnings("unused") View view) {
    new AlertDialog.Builder(this)
        .setTitle(R.string.dialog_title)
        .setMessage(R.string.dialog_message)
        .setNeutralButton("Fine", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int choice) {
            dialog.dismiss();
          }
        })
        .show();
    return true;
  }

  public boolean showPopupView(View view) {
    View content = getLayoutInflater().inflate(R.layout.popup_window, null, false);
    popupWindow = new PopupWindow(content, LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT, true);
    content.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        popupWindow.dismiss();
      }
    });

    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

    return true;
  }

  public boolean showPopupMenu(View view) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      return false;
    }

    PopupMenu popup = new PopupMenu(this, view);
    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.popup_menu, popup.getMenu());
    popup.show();
    return true;
  }

  public void pickContact(@SuppressWarnings("unused") View view) {
    Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
    pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PICK_CONTACT_REQUEST) {
      if (resultCode == RESULT_OK) {
        // TODO(user): hook this up for real as shown in this example:
        // http://developer.android.com/training/basics/intents/result.html
        TextView textView = (TextView) findViewById(R.id.phoneNumber);
        textView.setText(data.getExtras().getString("phone"));
      }
    }
  }
}
