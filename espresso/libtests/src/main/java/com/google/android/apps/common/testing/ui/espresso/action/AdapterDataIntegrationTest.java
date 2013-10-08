package com.google.android.apps.common.testing.ui.espresso.action;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.google.android.apps.common.testing.ui.testapp.LongListActivity;
import com.google.android.apps.common.testing.ui.testapp.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.Map;

/**
 * Integration tests for operating on data displayed in an adapter.
 */
@LargeTest
public class AdapterDataIntegrationTest extends ActivityInstrumentationTestCase2<LongListActivity> {
  @SuppressWarnings("deprecation")
  public AdapterDataIntegrationTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", LongListActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  @SuppressWarnings("unchecked")
  public void testClickAroundList() {
    onData(allOf(is(instanceOf(Map.class)), hasEntry(is(LongListActivity.STR), is("item: 99"))))
        .perform(click());
    onView(withId(R.id.selection_row_value))
        .check(matches(withText("99")));

    onData(allOf(is(instanceOf(Map.class)), hasEntry(is(LongListActivity.STR), is("item: 1"))))
        .perform(click());

    onView(withId(R.id.selection_row_value))
        .check(matches(withText("1")));

    onData(allOf(is(instanceOf(Map.class))))
        .atPosition(20)
        .perform(click());

    onView(withId(R.id.selection_row_value))
        .check(matches(withText("20")));

    // lets operate on a specific child of a row...
    onData(allOf(is(instanceOf(Map.class)), hasEntry(is(LongListActivity.STR), is("item: 50"))))
        .onChildView(withId(R.id.item_size))
        .perform(click())
        .check(matches(withText(String.valueOf("item: 50".length()))));

    onView(withId(R.id.selection_row_value))
        .check(matches(withText("50")));
  }

  @SuppressWarnings("unchecked")
  public void testSelectItemWithSibling() {
    onView(allOf(withText("7"), hasSibling(withText("item: 0"))))
        .perform(click());
    onView(withId(R.id.selection_row_value))
        .check(matches(withText("0")));
  }
}
