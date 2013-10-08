package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Simple activity used to demonstrate a simple Espresso test.
 */
public class SimpleActivity extends Activity implements OnItemSelectedListener{

  static final String EXTRA_DATA = "com.google.android.apps.common.testing.ui.testapp.DATA";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);

    Spinner spinner = (Spinner) findViewById(R.id.spinner_simple);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.spinner_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);
  }

  public void simpleButtonClicked(View view) {
    TextView textView = (TextView) findViewById(R.id.text_simple);
    String message = "Hello Espresso!";
    textView.setText(message);
  }

  /** Called when user clicks the Send button */
  public void sendButtonClicked(@SuppressWarnings("unused") View view) {
    Intent intent = new Intent(this, DisplayActivity.class);
    EditText editText = (EditText) findViewById(R.id.sendtext_simple);
    intent.putExtra(EXTRA_DATA, editText.getText().toString());
    startActivity(intent);
  }

  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    TextView textView = (TextView) findViewById(R.id.spinnertext_simple);
    textView.setText(String.format("One %s a day!", parent.getItemAtPosition(pos)));
  }

  public void onNothingSelected(AdapterView<?> parent) {
  }
}

