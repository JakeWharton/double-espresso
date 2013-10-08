package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.registerIdlingResources;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;

import com.google.android.apps.common.testing.ui.espresso.IdlingResource;
import com.google.android.apps.common.testing.ui.testapp.SendActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Integration test with IdlingResources.
 */
@LargeTest
public class IdlingResourceIntegrationTest extends ActivityInstrumentationTestCase2<SendActivity> {

  private ResettingIdlingResource r1;
  private ResettingIdlingResource r2;

  @SuppressWarnings("deprecation")
  public IdlingResourceIntegrationTest() {
    super("com.google.android.apps.common.testing.ui.testapp", SendActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    r1 = new ResettingIdlingResource("SlowResource", 6000);
    r2 = new ResettingIdlingResource("FastResource", 500);
    registerIdlingResources(r1, r2);
    getActivity();
  }

  public void testClickWithCustomIdlingResources() {
    onView(withText(equalToIgnoringCase("send"))).perform(click());
    r1.reset();
    r2.reset();
    onView(withText(is("Data from sender"))).check(matches(isDisplayed()));
    r1.reset();
    r2.reset();
    pressBack();
    r1.reset();
    r2.reset();
    onView(withText(equalToIgnoringCase("send"))).perform(click());
    r1.reset();
    r2.reset();
    pressBack();
    r1.reset();
    r2.reset();
    onView(withText(equalToIgnoringCase("send"))).perform(click());
  }

  private class ResettingIdlingResource implements IdlingResource {
    private final String name;
    private final long delay;
    private final AtomicBoolean isIdle = new AtomicBoolean(false);
    private final ScheduledExecutorService pool;
    
    private ResourceCallback callback;

    public ResettingIdlingResource(String name, long delay) {
      this.name = name;
      this.delay = delay;
      this.pool = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void registerIdleTransitionCallback(final ResourceCallback callback) {
      this.callback = callback;
      scheduleDelayedCallback();
    }

    private void scheduleDelayedCallback() {
      pool.schedule(new Runnable() {
        @Override
        public void run() {
          callback.onTransitionToIdle();
          isIdle.set(true);
        }
      }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isIdleNow() {
      return isIdle.get();
    }

    @Override
    public String getName() {
      return name;
    }

    public void reset() {
      isIdle.set(false);
      scheduleDelayedCallback();
    }
  }
}
