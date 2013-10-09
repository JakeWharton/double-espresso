package com.google.android.apps.common.testing.ui.testapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Shows ActionBar with a lot of items to get Action overflow on large displays. Click on item
 * changes text of R.id.textActionBarResult.
 */
public class ActionBarTestActivity extends ActionBarActivity {
  private ActionMode mode;
  private MenuInflater inflater;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actionbar_activity);
    inflater = getMenuInflater();
    mode = startSupportActionMode(new TestActionMode());

    ((Button) findViewById(R.id.show_contextual_action_bar)).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mode = startSupportActionMode(new TestActionMode());
          }
        });
    ((Button) findViewById(R.id.hide_contextual_action_bar)).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (mode != null) {
              mode.finish();
            }
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    inflater.inflate(R.menu.actionbar_context_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menu) {
    setResult(menu.getTitle());
    return true;
  }

  private void setResult(CharSequence result) {
    TextView text = (TextView) findViewById(R.id.text_action_bar_result);
    text.setText(result);
  }

  private final class TestActionMode implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      inflater.inflate(R.menu.actionbar_activity_actions, menu);
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      setResult(item.getTitle());
      return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}
  }
}
