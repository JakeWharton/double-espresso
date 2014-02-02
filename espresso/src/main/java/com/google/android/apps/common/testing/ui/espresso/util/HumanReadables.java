package com.google.android.apps.common.testing.ui.espresso.util;

import static com.google.android.apps.common.testing.ui.espresso.util.TreeIterables.depthFirstViewTraversalWithDistance;

import com.google.android.apps.common.testing.ui.espresso.util.TreeIterables.ViewAndDistance;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import android.content.res.Resources;
import android.os.Build;
import android.util.Printer;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Checkable;
import android.widget.TextView;

import java.util.List;

/**
 * Text converters for various Android objects.
 */
public final class HumanReadables {

  private HumanReadables() {}

  /**
   * Prints out an error message feature the view hierarchy starting at the rootView.
   *
   * @param rootView the root of the hierarchy tree to print out.
   * @param problemViews list of the views that you would like to point out are causing the error
   *        message.
   * @param errorHeader the header of the error message (should contain the description of why the
   *        error is happening).
   * @param problemViewSuffix the message to append to the view description in the tree printout.
   *        Required if problemViews is supplied.
   * @return a string for human consumption.
   */
  public static String getViewHierarchyErrorMessage(View rootView,
      final Optional<List<View>> problemViews, String errorHeader,
      final Optional<String> problemViewSuffix) {
    Preconditions.checkArgument(!problemViews.isPresent() || problemViewSuffix.isPresent());
    StringBuilder errorMessage = new StringBuilder(errorHeader);
    if (problemViewSuffix.isPresent()) {
      errorMessage.append(
          String.format("\nProblem views are marked with '%s' below.", problemViewSuffix.get()));
    }

    errorMessage.append("\n\nView Hierarchy:\n");

    Joiner.on("\n").appendTo(errorMessage, Iterables.transform(
        depthFirstViewTraversalWithDistance(rootView), new Function<ViewAndDistance, String>() {
          @Override
          public String apply(ViewAndDistance viewAndDistance) {
            String formatString = "+%s%s ";
            if (problemViews.isPresent()
                && problemViews.get().contains(viewAndDistance.getView())) {
              formatString += problemViewSuffix.get();
            }
            formatString += "\n|";

            return String.format(formatString,
                Strings.padStart(">", viewAndDistance.getDistanceFromRoot() + 1, '-'),
                HumanReadables.describe(viewAndDistance.getView()));
          }
        }));

    return errorMessage.toString();
  }

  /**
   * Transforms an arbitrary view into a string with (hopefully) enough debug info.
   *
   * @param v nullable view
   * @return a string for human consumption.
   */
  public static String describe(View v) {
    if (null == v) {
      return "null";
    }
    ToStringHelper helper = Objects.toStringHelper(v).add("id", v.getId());
    if (v.getId() != -1 && v.getResources() != null) {
      try {
        helper.add("res-name", v.getResources().getResourceEntryName(v.getId()));
      } catch (Resources.NotFoundException ignore) {
        // Do nothing.
      }
    }
    if (null != v.getContentDescription()) {
      helper.add("desc", v.getContentDescription());
    }

    switch (v.getVisibility()) {
      case View.GONE:
        helper.add("visibility", "GONE");
        break;
      case View.INVISIBLE:
        helper.add("visibility", "INVISIBLE");
        break;
      case View.VISIBLE:
        helper.add("visibility", "VISIBLE");
        break;
      default:
        helper.add("visibility", v.getVisibility());
    }

    helper.add("width", v.getWidth())
      .add("height", v.getHeight())
      .add("has-focus", v.hasFocus())
      .add("has-focusable", v.hasFocusable())
      .add("has-window-focus", v.hasWindowFocus())
      .add("is-clickable", v.isClickable())
      .add("is-enabled", v.isEnabled())
      .add("is-focused", v.isFocused())
      .add("is-focusable", v.isFocusable())
      .add("is-layout-requested", v.isLayoutRequested())
      .add("is-selected", v.isSelected());

    if (null != v.getRootView()) {
      // pretty much only true in unit-tests.
      helper.add("root-is-layout-requested", v.getRootView().isLayoutRequested());
    }

    EditorInfo ei = new EditorInfo();
    InputConnection ic = v.onCreateInputConnection(ei);
    boolean hasInputConnection = ic != null;
    helper.add("has-input-connection", hasInputConnection);
    if (hasInputConnection) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      Printer p = new StringBuilderPrinter(sb);
      ei.dump(p, "");
      sb.append("]");
      helper.add("editor-info", sb.toString().replace("\n", " "));
    }

    if (Build.VERSION.SDK_INT > 10) {
      helper.add("x", v.getX()).add("y", v.getY());
    }

    if (v instanceof TextView) {
      innerDescribe((TextView) v, helper);
    }
    if (v instanceof Checkable) {
      innerDescribe((Checkable) v, helper);
    }
    if (v instanceof ViewGroup) {
      innerDescribe((ViewGroup) v, helper);
    }
    return helper.toString();
  }

  private static void innerDescribe(TextView textBox, ToStringHelper helper) {
    if (null != textBox.getText()) {
      helper.add("text", textBox.getText());
    }

    if (null != textBox.getError()) {
      helper.add("error-text", textBox.getError());
    }

    if (null != textBox.getHint()) {
      helper.add("hint", textBox.getHint());
    }

    helper.add("input-type", textBox.getInputType());
    helper.add("ime-target", textBox.isInputMethodTarget());
  }

  private static void innerDescribe(Checkable checkable, ToStringHelper helper) {
    helper.add("is-checked", checkable.isChecked());
  }

  private static void innerDescribe(ViewGroup viewGroup, ToStringHelper helper) {
    helper.add("child-count", viewGroup.getChildCount());
  }
}
