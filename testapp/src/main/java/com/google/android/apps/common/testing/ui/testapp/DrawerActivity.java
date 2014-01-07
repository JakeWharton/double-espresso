package com.google.android.apps.common.testing.ui.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
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

    ActionBarDrawerToggle navigationDrawerToggle = new ActionBarDrawerToggle(
        this,
        drawerLayout,
        R.drawable.ic_launcher,
        R.string.nav_drawer_open,
        R.string.nav_drawer_close);
    drawerLayout.setDrawerListener(navigationDrawerToggle);
  }
}

