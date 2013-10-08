package com.google.android.apps.common.testing.ui.espresso.contrib;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.android.apps.common.testing.ui.espresso.IdlingResource.ResourceCallback;

import android.test.InstrumentationTestCase;

import org.mockito.Mock;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/** Unit tests for {@link CountingIdlingResource}. */
public class CountingIdlingResourceTest extends InstrumentationTestCase {

  private static final String RESOURCE_NAME = "test_resource";
  private CountingIdlingResource resource;

  @Mock
  private ResourceCallback mockCallback;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    initMocks(this);
    resource = new CountingIdlingResource(RESOURCE_NAME, true);
  }

  public void testResourceName() {
    assertEquals(RESOURCE_NAME, resource.getName());
  }

  public void testInvalidStateDetected() throws Exception {
    resource.increment();
    resource.decrement();
    try {
      resource.decrement();
      fail("Should throw illegal state exception!");
    } catch (IllegalStateException expected) { }
  }

  public void testIsIdle() throws Exception {
    assertTrue(callIsIdle());
    resource.increment();
    assertFalse(callIsIdle());
    resource.decrement();
    assertTrue(callIsIdle());
  }

  public void testIdleNotification() throws Exception {
    registerIdleCallback();
    assertTrue(callIsIdle());
    verify(mockCallback, never()).onTransitionToIdle();

    resource.increment();
    verify(mockCallback, never()).onTransitionToIdle();
    assertFalse(callIsIdle());

    resource.decrement();
    verify(mockCallback).onTransitionToIdle();
    assertTrue(callIsIdle());
  }

  private void registerIdleCallback() throws Exception {
    FutureTask<Void> registerTask = new FutureTask<Void>(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        resource.registerIdleTransitionCallback(mockCallback);
        return null;
      }

    });
    getInstrumentation().runOnMainSync(registerTask);
    try {
      registerTask.get();
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }

  }

  private boolean callIsIdle() throws Exception {
    FutureTask<Boolean> isIdleTask = new FutureTask<Boolean>(new IsIdleCallable());
    getInstrumentation().runOnMainSync(isIdleTask);
    try {
      return isIdleTask.get();
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }
  }


  private class IsIdleCallable implements Callable<Boolean> {
    @Override
    public Boolean call() throws Exception {
      return resource.isIdleNow();
    }
  }

}
