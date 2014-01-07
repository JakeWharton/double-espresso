package com.google.android.apps.common.testing.ui.espresso;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import android.test.AndroidTestCase;
import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link NoMatchingViewException}. */
public class NoMatchingViewExceptionTest extends AndroidTestCase {
  private Matcher<View> alwaysFailingMatcher;

  @Mock
  private View testView;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    alwaysFailingMatcher = Matchers.<View>nullValue();
  }

  public void testExceptionContainsMatcherDescription() {
    StringBuilder matcherDescription = new StringBuilder();
    alwaysFailingMatcher.describeTo(new StringDescription(matcherDescription));
    assertThat(createException().getMessage(), containsString(matcherDescription.toString()));
  }

  public void testExceptionContainsView() {
    String exceptionMessage = createException().getMessage();

    assertThat("missing root element" + exceptionMessage, exceptionMessage,
        containsString("{id=0,"));
  }

  private NoMatchingViewException createException() {
    return new NoMatchingViewException.Builder()
        .withViewMatcher(alwaysFailingMatcher)
        .withRootView(testView)
        .build();
  }
}
