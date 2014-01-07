package com.google.android.apps.common.testing.ui.espresso;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;

/** Unit tests for {@link AmbiguousViewMatcherException}. */
public class AmbiguousViewMatcherExceptionTest extends AndroidTestCase {
  private Matcher<View> alwaysTrueMatcher;

  private RelativeLayout testView;
  private View child1;
  private View child2;
  private View child3;
  private View child4;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    alwaysTrueMatcher = Matchers.<View>notNullValue();
    testView = new RelativeLayout(getContext());
    child1 = new TextView(getContext());
    child1.setId(1);
    child2 = new TextView(getContext());
    child2.setId(2);
    child3 = new TextView(getContext());
    child3.setId(3);
    child4 = new TextView(getContext());
    child4.setId(4);
    testView.addView(child1);
    testView.addView(child2);
    testView.addView(child3);
    testView.addView(child4);
  }

  public void testExceptionContainsMatcherDescription() {
    StringBuilder matcherDescription = new StringBuilder();
    alwaysTrueMatcher.describeTo(new StringDescription(matcherDescription));
    assertThat(createException().getMessage(), containsString(matcherDescription.toString()));
  }

  @SuppressWarnings("unchecked")
  public void testExceptionContainsView() {
    String exceptionMessage = createException().getMessage();

    assertThat("missing elements", exceptionMessage,
       allOf(
         containsString("{id=1,"), // child1
         containsString("{id=2,"), // child2
         containsString("{id=3,"), // child3
         containsString("{id=4,"), // child4
         containsString("{id=-1,"))); // root
  }

  private AmbiguousViewMatcherException createException() {

    return new AmbiguousViewMatcherException.Builder()
        .withViewMatcher(alwaysTrueMatcher)
        .withRootView(testView)
        .withView1(testView)
        .withView2(child1)
        .withOtherAmbiguousViews(child2, child3, child4)
        .build();
  }
}
