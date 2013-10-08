package com.google.android.apps.common.testing.ui.espresso;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

import com.google.android.apps.common.testing.ui.testapp.R;
import com.google.android.apps.common.testing.ui.testapp.SyncActivity;

import android.os.Handler;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test case for {@link AppNotIdleException}.
 */
@LargeTest
public class AppNotIdleExceptionTest extends ActivityInstrumentationTestCase2<SyncActivity> {

  @SuppressWarnings("deprecation")
  public AppNotIdleExceptionTest() {
    // This constructor was deprecated - but we want to support lower API levels.
    super("com.google.android.apps.common.testing.ui.testapp", SyncActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testAppIdleException() throws Exception {
    final AtomicBoolean continueBeingBusy = new AtomicBoolean(true);
    try {
      final Handler handler = new Handler(Looper.getMainLooper());
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          if (!continueBeingBusy.get()) {
            return;
          } else {
            handler.post(this);
          }
        }
      };
      FutureTask<Void> task = new FutureTask<Void>(runnable, null);
      handler.post(task);
      task.get(); // Will Make sure that the first post is sent before we do a lookup.
      // Request the "hello world!" text by clicking on the request button.
      onView(withId(R.id.request_button)).perform(click());
      fail("Espresso failed to throw AppNotIdleException");
    } catch (AppNotIdleException e) {
      // Do Nothing. Test pass.
      continueBeingBusy.getAndSet(false);
    }
  }
}
