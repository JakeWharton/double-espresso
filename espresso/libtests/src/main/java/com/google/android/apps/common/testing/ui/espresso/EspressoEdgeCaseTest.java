package com.google.android.apps.common.testing.ui.espresso;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Collection of some nasty edge cases.
 */
@LargeTest
public class EspressoEdgeCaseTest extends ActivityInstrumentationTestCase2<SendActivity> {
  @SuppressWarnings("deprecation")
  public EspressoEdgeCaseTest() {
    // Supporting froyo.
    super("com.google.android.apps.common.testing.ui.testapp", SendActivity.class);
  }

  private static final Callable<Void> NO_OP = new Callable<Void>() {
    @Override
    public Void call() {
      return null;
    }
  };

  private Handler mainHandler;
  private OneShotResource oneShotResource;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
    mainHandler = new Handler(Looper.getMainLooper());
    oneShotResource = new OneShotResource();
  }

  @Override
  public void tearDown() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
    IdlingPolicies.setIdlingResourceTimeout(26, TimeUnit.SECONDS);
    oneShotResource.setIdle(true);
    super.tearDown();
  }

  @SuppressWarnings("unchecked")
  public void testRecoveryFromExceptionOnMainThreadLoopMainThreadUntilIdle() throws Exception {
    final RuntimeException poison = new RuntimeException("oops");
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {

                @Override
                public void perform(UiController controller, View view) {
                  mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                      throw poison;
                    }});
                  controller.loopMainThreadUntilIdle();
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re == poison) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text))
        .perform(typeText("Hello World111"));
    onView(withId(R.id.enter_data_edit_text))
        .check(matches(withText("Hello World111")));
  }

  @SuppressWarnings("unchecked")
  public void testRecoveryFromExceptionOnMainThreadLoopMainThreadForAtLeast() throws Exception {
    final RuntimeException poison = new RuntimeException("oops");
    final FutureTask<Void> syncTask = new FutureTask<Void>(NO_OP);
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {
                @Override
                public void perform(UiController controller, View view) {
                  mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                      throw poison;
                    }});
                  // block test execution until loopMainThreadForAtLeast call
                  // would be satisified
                  mainHandler.postDelayed(syncTask, 2500);
                  controller.loopMainThreadForAtLeast(2000);
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re == poison) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    syncTask.get();

    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text))
        .perform(typeText("baz bar"));
    onView(withId(R.id.enter_data_edit_text))
        .check(matches(withText("baz bar")));
  }

  @SuppressWarnings("unchecked")
  public void testRecoveryFromTimeOutExceptionMaster() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.SECONDS);
    final FutureTask<Void> syncTask = new FutureTask<Void>(NO_OP);
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {
                @Override
                public void perform(UiController controller, View view) {
                  mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                      SystemClock.sleep(TimeUnit.SECONDS.toMillis(8));
                    }
                  });
                  // block test execution until loopMainThreadForAtLeast call
                  // would be satisified
                  mainHandler.postDelayed(syncTask, 2500);
                  controller.loopMainThreadForAtLeast(1000);
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re instanceof EspressoException) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    syncTask.get();

    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text))
        .perform(typeText("one two three"));
    onView(withId(R.id.enter_data_edit_text))
        .check(matches(withText("one two three")));
  }

  @SuppressWarnings("unchecked")
  public void testRecoveryFromTimeOutExceptionDynamic() {
    IdlingPolicies.setIdlingResourceTimeout(2, TimeUnit.SECONDS);

    Espresso.registerIdlingResources(oneShotResource);
    oneShotResource.setIdle(false);

    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(click());
      fail("should throw");
    } catch (RuntimeException re) {
      if (re instanceof EspressoException) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    oneShotResource.setIdle(true);

    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text))
        .perform(typeText("Doh"));
    onView(withId(R.id.enter_data_edit_text))
        .check(matches(withText("Doh")));
  }

  public void testRecoveryFromAsyncTaskTimeout() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.SECONDS);
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(new TestAction() {
            @Override
            public void perform(UiController controller, View view) {
              new AsyncTask<Void, Void, Void>() {
                @Override
                public Void doInBackground(Void... params) {
                  SystemClock.sleep(TimeUnit.SECONDS.toMillis(8));
                  return null;
                }
              }.execute();
              // block test execution until loopMainThreadForAtLeast call
              // would be satisified
              controller.loopMainThreadForAtLeast(1000);
            }
          });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re instanceof EspressoException) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text))
        .perform(typeText("Har Har"));
    onView(withId(R.id.enter_data_edit_text))
        .check(matches(withText("Har Har")));
  }




  private abstract static class TestAction implements ViewAction {
    @Override
    public String getDescription() {
      return "A random test action.";
    }

    @Override
    public Matcher<View> getConstraints() {
      return isAssignableFrom(View.class);
    }
  }


  private static class OneShotResource implements IdlingResource {
    private static AtomicInteger counter = new AtomicInteger(0);

    private final int instance;
    private volatile IdlingResource.ResourceCallback callback;
    private volatile boolean isIdle = true;

    private OneShotResource() {
      instance = counter.incrementAndGet();
    }

    @Override
    public String getName() {
      return "TestOneShotResource_" + counter;
    }

    public void setIdle(boolean idle) {
      isIdle = idle;
      if (isIdle && callback != null) {
        callback.onTransitionToIdle();
      }
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(IdlingResource.ResourceCallback callback) {
      this.callback = callback;
    }
  }
}
