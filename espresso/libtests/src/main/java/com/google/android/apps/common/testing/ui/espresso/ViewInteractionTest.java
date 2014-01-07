package com.google.android.apps.common.testing.ui.espresso;

import static com.google.common.base.Throwables.propagate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitor;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.MoreExecutors;

import android.test.AndroidTestCase;
import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.mockito.Mock;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/** Unit tests for {@link ViewInteraction}. */
public class ViewInteractionTest extends AndroidTestCase {
  @Mock
  private ViewFinder mockViewFinder;
  @Mock
  private ViewAssertion mockAssertion;
  @Mock
  private ViewAction mockAction;
  @Mock
  private UiController mockUiController;

  
  private FailureHandler failureHandler;
  private Executor testExecutor = MoreExecutors.sameThreadExecutor();

  private ActivityLifecycleMonitor realLifecycleMonitor;
  private ViewInteraction testInteraction;
  private View rootView;
  private View targetView;
  private Matcher<View> viewMatcher;
  private Matcher<View> actionConstraint;
  private AtomicReference<Matcher<Root>> rootMatcherRef;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    initMocks(this);
    realLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance();
    rootView = new View(getContext());
    targetView = new View(getContext());
    viewMatcher = is(targetView);
    actionConstraint = Matchers.<View>notNullValue();
    rootMatcherRef = new AtomicReference<Matcher<Root>>(RootMatchers.DEFAULT);
    when(mockAction.getDescription()).thenReturn("A Mock!");
    failureHandler = new FailureHandler() {
      @Override
      public void handle(Throwable error, Matcher<View> viewMatcher) {
        propagate(error);
      }
    };
  }

  @Override
  public void tearDown() throws Exception {
    ActivityLifecycleMonitorRegistry.registerInstance(realLifecycleMonitor);
    super.tearDown();
  }

  public void testPerformViewViolatesConstraints() {
    actionConstraint = not(viewMatcher);
    when(mockViewFinder.getView()).thenReturn(targetView);
    initInteraction();
    try {
      testInteraction.perform(mockAction);
      fail("should propagate constraint violation!");
    } catch (RuntimeException re) {
      if (!PerformException.class.isAssignableFrom(re.getClass())) {
        throw re;
      }
    }
  }

  public void testPerformPropagatesException() {
    RuntimeException exceptionToRaise = new RuntimeException();
    when(mockViewFinder.getView()).thenReturn(targetView);
    doThrow(exceptionToRaise)
        .when(mockAction)
        .perform(mockUiController, targetView);
    initInteraction();
    try {
      testInteraction.perform(mockAction);
      fail("Should propagate exception stored in view operation!");
    } catch (RuntimeException re) {
      verify(mockAction).perform(mockUiController, targetView);
      assertThat(exceptionToRaise, is(re));
    }
  }

  public void testCheckPropagatesException() {
    RuntimeException exceptionToRaise = new RuntimeException();
    when(mockViewFinder.getView()).thenReturn(targetView);
    doThrow(exceptionToRaise)
      .when(mockAssertion)
      .check(Optional.of(targetView), Optional.<NoMatchingViewException>absent());

    initInteraction();
    try {
      testInteraction.check(mockAssertion);
      fail("Should propagate exception stored in view operation!");
    } catch (RuntimeException re) {
      verify(mockAssertion).check(Optional.of(targetView),
          Optional.<NoMatchingViewException>absent());
      assertThat(exceptionToRaise, is(re));
    }
  }

  public void testPerformTwiceUpdatesPreviouslyMatched() {
    View firstView = new View(getContext());
    View secondView = new View(getContext());
    when(mockViewFinder.getView()).thenReturn(firstView);
    initInteraction();
    testInteraction.perform(mockAction);
    verify(mockAction).perform(mockUiController, firstView);

    when(mockViewFinder.getView()).thenReturn(secondView);
    testInteraction.perform(mockAction);
    verify(mockAction).perform(mockUiController, secondView);

    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(Optional.of(secondView),
          Optional.<NoMatchingViewException>absent());

  }

  public void testPerformAndCheck() {
    when(mockViewFinder.getView()).thenReturn(targetView);
    initInteraction();
    testInteraction.perform(mockAction);
    verify(mockAction).perform(mockUiController, targetView);

    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(Optional.of(targetView),
          Optional.<NoMatchingViewException>absent());
  }

  public void testCheck() {
    when(mockViewFinder.getView()).thenReturn(targetView);
    initInteraction();
    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(Optional.of(targetView),
          Optional.<NoMatchingViewException>absent());
  }

  public void testInRootUpdatesRef() {
    initInteraction();
    Matcher<Root> testMatcher = nullValue();
    testInteraction.inRoot(testMatcher);
    assertEquals(testMatcher, rootMatcherRef.get());
  }

  public void testInRoot_NullHandling() {
    initInteraction();
    try {
      testInteraction.inRoot(null);
      fail("should throw");
    } catch (NullPointerException expected) {
    }
  }

  public void testCheck_ViewCannotBeFound() {
    NoMatchingViewException noViewException = new NoMatchingViewException.Builder()
        .withViewMatcher(viewMatcher)
        .withRootView(rootView)
        .build();

    when(mockViewFinder.getView()).thenThrow(noViewException);
    initInteraction();
    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(Optional.<View>absent(), Optional.of(noViewException));
  }

  private void initInteraction() {
    when(mockAction.getConstraints()).thenReturn(actionConstraint);

    testInteraction = new ViewInteraction(mockUiController, mockViewFinder, testExecutor,
        failureHandler, viewMatcher, rootMatcherRef);

  }
}
