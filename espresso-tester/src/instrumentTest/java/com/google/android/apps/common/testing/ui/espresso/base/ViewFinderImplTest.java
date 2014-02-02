package com.google.android.apps.common.testing.ui.espresso.base;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.android.apps.common.testing.ui.espresso.AmbiguousViewMatcherException;
import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.android.apps.common.testing.ui.espresso.ViewFinder;

import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hamcrest.Matchers;

import javax.inject.Provider;

/** Unit tests for {@link ViewFinderImpl}. */
public class ViewFinderImplTest extends InstrumentationTestCase {
  private Provider<View> testViewProvider;
  private RelativeLayout testView;
  private View child1;
  private View child2;
  private View child3;
  private View child4;
  private View nestedChild;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    testView = new RelativeLayout(getInstrumentation().getTargetContext());
    child1 = new TextView(getInstrumentation().getTargetContext());
    child1.setId(1);
    child2 = new TextView(getInstrumentation().getTargetContext());
    child2.setId(2);
    child3 = new TextView(getInstrumentation().getTargetContext());
    child3.setId(3);
    child4 = new TextView(getInstrumentation().getTargetContext());
    child4.setId(4);
    nestedChild = new TextView(getInstrumentation().getTargetContext());
    nestedChild.setId(5);
    RelativeLayout nestingLayout = new RelativeLayout(getInstrumentation().getTargetContext());
    nestingLayout.addView(nestedChild);
    testView.addView(child1);
    testView.addView(child2);
    testView.addView(nestingLayout);
    testView.addView(child3);
    testView.addView(child4);
    testViewProvider = new Provider<View>() {
      @Override
      public View get() {
        return testView;
      }

      @Override
      public String toString() {
        return "of(" + testView + ")";
      }
    };
  }

  @UiThreadTest
  public void testGetView_present() {
    ViewFinder finder = new ViewFinderImpl(sameInstance(nestedChild), testViewProvider);
    assertThat(finder.getView(), sameInstance(nestedChild));
  }

  @UiThreadTest
  public void testGetView_missing() {
    ViewFinder finder = new ViewFinderImpl(Matchers.<View>nullValue(), testViewProvider);
    try {
      finder.getView();
      fail("No children should pass that matcher!");
    } catch (NoMatchingViewException expected) {}
  }

  @UiThreadTest
  public void testGetView_multiple() {
    ViewFinder finder = new ViewFinderImpl(Matchers.<View>notNullValue(), testViewProvider);
    try {
      finder.getView();
      fail("All nodes hit that matcher!");
    } catch (AmbiguousViewMatcherException expected) {}
  }

  public void testFind_offUiThread() {
    ViewFinder finder = new ViewFinderImpl(sameInstance(nestedChild), testViewProvider);
    try {
      finder.getView();
      fail("not on main thread, should die.");
    } catch (IllegalStateException expected) {}
  }

}
