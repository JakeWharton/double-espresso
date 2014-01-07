package com.google.android.apps.common.testing.ui.espresso.sample;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.android.apps.common.testing.ui.espresso.sample.LongListMatchers.isFooter;
import static com.google.android.apps.common.testing.ui.espresso.sample.LongListMatchers.withItemContent;
import static com.google.android.apps.common.testing.ui.espresso.sample.LongListMatchers.withItemSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.android.apps.common.testing.ui.testapp.LongListActivity;
import com.google.android.apps.common.testing.ui.testapp.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Demonstrates the usage of
 * {@link com.google.android.apps.common.testing.ui.espresso.Espresso#onData(org.hamcrest.Matcher)}
 * to match data within list views.
 */
@LargeTest
public class AdapterViewTest extends ActivityInstrumentationTestCase2<LongListActivity> {

  @SuppressWarnings("deprecation")
  public AdapterViewTest() {
    // This constructor was deprecated - but we want to support lower API levels.
    super("com.google.android.apps.common.testing.ui.testapp", LongListActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testClickOnItem50() {
    // The text view "item: 50" may not exist if we haven't scrolled to it.
    // By using onData api we tell Espresso to look into the Adapter for an item matching
    // the matcher we provide it. Espresso will then bring that item into the view hierarchy
    // and we can click on it.

    onData(withItemContent("item: 50"))
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("50")));
  }

  public void testClickOnSpecificChildOfRow60() {
    onData(withItemContent("item: 60"))
      .onChildView(withId(R.id.item_size)) // resource id of second column from xml layout
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("60")));

    onView(withId(R.id.selection_column_value))
      .check(matches(withText("2")));
  }

  public void testClickOnFirstAndFifthItemOfLength8() {
    onData(is(withItemSize(8)))
      .atPosition(0)
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("10")));

    onData(is(withItemSize(8)))
      .atPosition(4)
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("14")));
  }

  @SuppressWarnings("unchecked")
  public void testClickFooter() {
    onData(isFooter())
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("100")));
  }

  @SuppressWarnings("unchecked")
  public void testDataItemNotInAdapter(){
    onView(withId(R.id.list))
      .check(matches(not(withAdaptedData(withItemContent("item: 168")))));
  }

  private static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
    return new TypeSafeMatcher<View>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("with class name: ");
        dataMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(View view) {
        if (!(view instanceof AdapterView)) {
          return false;
        }
        @SuppressWarnings("rawtypes")
        Adapter adapter = ((AdapterView) view).getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
          if (dataMatcher.matches(adapter.getItem(i))) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
