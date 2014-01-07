package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity to demonstrate actions on a {@link DrawerLayout}.
 */
public class DrawerActivity extends Activity {

  public static final String[] DRAWER_CONTENTS =
      new String[] {"Platypus", "Wombat", "Pickle", "Badger"};

  private ActionBarDrawerToggle drawerToggle;
  private CharSequence title;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawer_activity);

    ListAdapter listAdapter = new ArrayAdapter<String>(
        getApplicationContext(), R.layout.drawer_row, R.id.drawer_row_name, DRAWER_CONTENTS);
    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ListView drawerList = (ListView) findViewById(R.id.drawer_list);
    drawerList.setAdapter(listAdapter);

    final TextView textView = (TextView) findViewById(R.id.drawer_text_view);

    drawerList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        textView.setText("You picked: " + DRAWER_CONTENTS[(int) id]);
        drawerLayout.closeDrawers();
      }
    });

    // enable ActionBar app icon to behave as action to toggle nav drawer
    // TODO(user): use compat lib for lower API levels
    if (android.os.Build.VERSION.SDK_INT >= 11) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
    }

    title = getTitle();

    drawerToggle = new ActionBarDrawerToggle(
        this,
        drawerLayout,
        R.drawable.ic_drawer,
        R.string.nav_drawer_open,
        R.string.nav_drawer_close) {

        /** Called when a drawer has settled in a completely closed state. */
        public void onDrawerClosed(View view) {
          if (android.os.Build.VERSION.SDK_INT >= 11) {
            getActionBar().setTitle(title);
          }
        }

        /** Called when a drawer has settled in a completely open state. */
        public void onDrawerOpened(View drawerView) {
          if (android.os.Build.VERSION.SDK_INT >= 11) {
            getActionBar().setTitle(title);
          }
        }
    };
    drawerLayout.setDrawerListener(drawerToggle);
  }

  @Override
  public void setTitle(CharSequence title) {
    this.title = title;
    if (android.os.Build.VERSION.SDK_INT >= 11) {
      getActionBar().setTitle(title);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggls
    drawerToggle.onConfigurationChanged(newConfig);
  }
}
