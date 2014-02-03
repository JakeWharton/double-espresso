package com.google.android.apps.common.testing.ui.testapp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * An activity displaying a long list.
 */
public class LongListActivity extends Activity {

  @VisibleForTesting
  public static final String STR = "STR";
  @VisibleForTesting
  public static final String LEN = "LEN";
  @VisibleForTesting
  public static final String FOOTER = "FOOTER";

  private List<Map<String, Object>> data = Lists.newArrayList();
  private LayoutInflater layoutInflater;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    populateData();
    setContentView(R.layout.list_activity);
    ((TextView) findViewById(R.id.selection_row_value)).setText("");
    ((TextView) findViewById(R.id.selection_column_value)).setText("");

    ListView listView = (ListView) findViewById(R.id.list);
    String[] from = new String[] {STR, LEN};
    int[] to = new int[] {R.id.item_content, R.id.item_size};
    layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ListAdapter adapter = new SimpleAdapter(this, data, R.layout.list_item, from, to) {
      @Override
      public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
          convertView = layoutInflater.inflate(R.layout.list_item, null);
        }

        TextView textViewOne = (TextView) convertView.findViewById(R.id.item_content);
        if (textViewOne != null) {
          textViewOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              ((TextView) findViewById(R.id.selection_row_value)).setText(String.valueOf(position));
              ((TextView) findViewById(R.id.selection_column_value)).setText("1");
            }
          });
        }

        TextView textViewTwo = (TextView) convertView.findViewById(R.id.item_size);
        if (textViewTwo != null) {
          textViewTwo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              ((TextView) findViewById(R.id.selection_row_value)).setText(String.valueOf(position));
              ((TextView) findViewById(R.id.selection_column_value)).setText("2");
            }
          });
        }
        return super.getView(position, convertView, parent);
      }
    };

    View footerView = layoutInflater.inflate(R.layout.list_item, listView, false);
    ((TextView) footerView.findViewById(R.id.item_content)).setText("count:");
    ((TextView) footerView.findViewById(R.id.item_size)).setText(String.valueOf(data.size()));
    listView.addFooterView(footerView, FOOTER, true);

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(
          AdapterView<?> unusedParent, View clickedView, int position, long id) {
        ((TextView) findViewById(R.id.selection_column_value)).setText("");
        ((TextView) findViewById(R.id.selection_row_value)).setText(String.valueOf(position));
      }
    });
  }

  public Map<String, Object> makeItem(int forRow) {
    Map<String, Object> dataRow = Maps.newHashMap();
    dataRow.put(STR, "item: " + forRow);
    dataRow.put(LEN, ((String) dataRow.get(STR)).length());
    return dataRow;
  }

  private void populateData() {
    for (int i = 0; i < 100; i++) {
      data.add(makeItem(i));
    }
  }

}
