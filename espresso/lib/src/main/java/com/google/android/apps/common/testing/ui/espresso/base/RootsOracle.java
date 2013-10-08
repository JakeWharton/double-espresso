package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkState;

import com.google.android.apps.common.testing.ui.espresso.Root;
import com.google.common.collect.Lists;

import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides access to all root views in an application.
 *
 * 95% of the time this is unnecessary and we can operate solely on current Activity's root view
 * as indicated by getWindow().getDecorView(). However in the case of popup windows, menus, and
 * dialogs the actual view hierarchy we should be operating on is not accessible thru public apis.
 *
 * In the spirit of degrading gracefully when new api levels break compatibility, callers should
 * handle a list of size 0 by assuming getWindow().getDecorView() on the currently resumed activity
 * is the sole root - this assumption will be correct often enough.
 *
 * Obviously, you need to be on the main thread to use this.
 */
@Singleton
final class RootsOracle implements Provider<List<Root>> {

  private static final String TAG = RootsOracle.class.getSimpleName();
  private static final String WINDOW_MANAGER_IMPL_CLAZZ =
      "android.view.WindowManagerImpl";
  private static final String WINDOW_MANAGER_GLOBAL_CLAZZ =
      "android.view.WindowManagerGlobal";
  private static final String VIEWS_FIELD = "mViews";
  private static final String WINDOW_PARAMS_FIELD = "mParams";
  private static final String GET_DEFAULT_IMPL = "getDefault";
  private static final String GET_GLOBAL_INSTANCE = "getInstance";

  private final Looper mainLooper;
  private boolean initialized;
  private Object windowManagerObj;
  private Field viewsField;
  private Field paramsField;

  @Inject
  RootsOracle(Looper mainLooper) {
    this.mainLooper = mainLooper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Root> get() {
    checkState(mainLooper.equals(Looper.myLooper()), "must be called on main thread.");

    if (!initialized) {
      initialize();
    }

    if (null == windowManagerObj) {
      Log.w(TAG, "No reflective access to windowmanager object.");
      return Lists.newArrayList();
    }

    if (null == viewsField) {
      Log.w(TAG, "No reflective access to mViews");
      return Lists.newArrayList();
    }
    if (null == paramsField) {
      Log.w(TAG, "No reflective access to mPArams");
      return Lists.newArrayList();
    }

    List<View> views = null;
    List<LayoutParams> params = null;

    try {
      if (Build.VERSION.SDK_INT < 19) {
        views = Arrays.asList((View[]) viewsField.get(windowManagerObj));
        params = Arrays.asList((LayoutParams[]) paramsField.get(windowManagerObj));
      } else {
        views = (List<View>) viewsField.get(windowManagerObj);
        params = (List<LayoutParams>) paramsField.get(windowManagerObj);
      }
    } catch (RuntimeException re) {
      Log.w(TAG, String.format("Reflective access to %s or %s on %s failed.",
          viewsField, paramsField, windowManagerObj), re);
      return Lists.newArrayList();
    } catch (IllegalAccessException iae) {
      Log.w(TAG, String.format("Reflective access to %s or %s on %s failed.",
          viewsField, paramsField, windowManagerObj), iae);
      return Lists.newArrayList();
    }


    List<Root> roots = Lists.newArrayList();
    for (int i = views.size() - 1; i > -1; i--) {
      roots.add(
          new Root.Builder()
              .withDecorView(views.get(i))
              .withWindowLayoutParams(params.get(i))
              .build());
    }

    return roots;
  }

  private void initialize() {
    initialized = true;
    String accessClass = Build.VERSION.SDK_INT > 16 ? WINDOW_MANAGER_GLOBAL_CLAZZ
        : WINDOW_MANAGER_IMPL_CLAZZ;
    String instanceMethod = Build.VERSION.SDK_INT > 16 ? GET_GLOBAL_INSTANCE : GET_DEFAULT_IMPL;

    try {
      Class<?> clazz = Class.forName(accessClass);
      Method getMethod = clazz.getMethod(instanceMethod);
      windowManagerObj = getMethod.invoke(null);
      viewsField = clazz.getDeclaredField(VIEWS_FIELD);
      viewsField.setAccessible(true);
      paramsField = clazz.getDeclaredField(WINDOW_PARAMS_FIELD);
      paramsField.setAccessible(true);
    } catch (InvocationTargetException ite) {
      Log.e(TAG, String.format("could not invoke: %s on %s", instanceMethod, accessClass),
        ite.getCause());
    } catch (ClassNotFoundException cnfe) {
      Log.e(TAG, String.format("could not find class: %s", accessClass), cnfe);
    } catch (NoSuchFieldException nsfe) {
      Log.e(TAG, String.format("could not find field: %s or %s on %s", WINDOW_PARAMS_FIELD,
          VIEWS_FIELD, accessClass), nsfe);
    } catch (NoSuchMethodException nsme) {
      Log.e(TAG, String.format("could not find method: %s on %s", instanceMethod, accessClass),
        nsme);
    } catch (RuntimeException re) {
      Log.e(TAG, String.format("reflective setup failed using obj: %s method: %s field: %s",
        accessClass, instanceMethod, VIEWS_FIELD), re);
    } catch (IllegalAccessException iae) {
      Log.e(TAG, String.format("reflective setup failed using obj: %s method: %s field: %s",
        accessClass, instanceMethod, VIEWS_FIELD), iae);
    }
  }
}
