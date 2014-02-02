package com.google.android.apps.common.testing.ui.espresso;


import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;

import com.google.android.apps.common.testing.ui.espresso.action.AdapterDataLoaderAction;
import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocol;
import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocol.AdaptedData;
import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocols;
import com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers;
import com.google.common.base.Optional;

import android.view.View;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * An interface to interact with data displayed in AdapterViews.
 * <p>
 * This interface builds on top of {@link ViewInteraction} and should be the preferred way to
 * interact with elements displayed inside AdapterViews.
 * </p>
 * <p>
 * This is necessary because an AdapterView may not load all the data held by its Adapter into the
 * view hierarchy until a user interaction makes it necessary. Also it is more fluent / less brittle
 * to match upon the data object being rendered into the display then the rendering itself.
 * </p>
 * <p>
 * By default, a DataInteraction takes place against any AdapterView found within the current
 * screen, if you have multiple AdapterView objects displayed, you will need to narrow the selection
 * by using the inAdapterView method.
 * </p>
 * <p>
 * The check and perform method operate on the top level child of the adapter view, if you need to
 * operate on a subview (eg: a Button within the list) use the onChildView method before calling
 * perform or check.
 * </p>
 *
 */
public class DataInteraction {

  private final Matcher<Object> dataMatcher;
  private Matcher<View> adapterMatcher = isAssignableFrom(AdapterView.class);
  private Optional<Matcher<View>> childViewMatcher = Optional.absent();
  private Optional<Integer> atPosition = Optional.absent();
  private AdapterViewProtocol adapterViewProtocol = AdapterViewProtocols.standardProtocol();
  private Matcher<Root> rootMatcher = RootMatchers.DEFAULT;

  DataInteraction(Matcher<Object> dataMatcher) {
    this.dataMatcher = checkNotNull(dataMatcher);
  }

  /**
   * Causes perform and check methods to take place on a specific child view of the view returned
   * by Adapter.getView()
   */
  public DataInteraction onChildView(Matcher<View> childMatcher) {
    this.childViewMatcher = Optional.of(checkNotNull(childMatcher));
    return this;
  }

  /**
   * Causes this data interaction to work within the Root specified by the given root matcher.
   */
  public DataInteraction inRoot(Matcher<Root> rootMatcher) {
    this.rootMatcher = checkNotNull(rootMatcher);
    return this;
  }

  /**
   * Selects a particular adapter view to operate on, by default we operate on any adapter view
   * on the screen.
   */
  public DataInteraction inAdapterView(Matcher<View> adapterMatcher) {
    this.adapterMatcher = checkNotNull(adapterMatcher);
    return this;
  }

  /**
   * Selects the view which matches the nth position on the adapter
   * based on the data matcher.
   */
  public DataInteraction atPosition(Integer atPosition) {
    this.atPosition = Optional.of(checkNotNull(atPosition));
    return this;
  }

  /**
   * Use a different AdapterViewProtocol if the Adapter implementation does not
   * satisfy the AdapterView contract like (@code ExpandableListView)
   */
  public DataInteraction usingAdapterViewProtocol(AdapterViewProtocol adapterViewProtocol) {
    this.adapterViewProtocol = checkNotNull(adapterViewProtocol);
    return this;
  }

  /**
   * Performs an action on the view after we force the data to be loaded.
   *
   * @return an {@link ViewInteraction} for more assertions or actions.
   */
  public ViewInteraction perform(ViewAction... actions) {
     AdapterDataLoaderAction adapterDataLoaderAction = load();

    return onView(makeTargetMatcher(adapterDataLoaderAction))
        .inRoot(rootMatcher)
        .perform(actions);
  }

  /**
   * Performs an assertion on the state of the view after we force the data to be loaded.
   *
   * @return an {@link ViewInteraction} for more assertions or actions.
   */
  public ViewInteraction check(ViewAssertion assertion) {
     AdapterDataLoaderAction adapterDataLoaderAction = load();

    return onView(makeTargetMatcher(adapterDataLoaderAction))
        .inRoot(rootMatcher)
        .check(assertion);
  }

  private AdapterDataLoaderAction load() {
    AdapterDataLoaderAction adapterDataLoaderAction =
       new AdapterDataLoaderAction(dataMatcher, atPosition, adapterViewProtocol);
    onView(adapterMatcher)
      .inRoot(rootMatcher)
      .perform(adapterDataLoaderAction);
    return adapterDataLoaderAction;
  }

  @SuppressWarnings("unchecked")
  private Matcher<View> makeTargetMatcher(AdapterDataLoaderAction adapterDataLoaderAction) {
    Matcher<View> targetView = displayingData(adapterMatcher, dataMatcher, adapterViewProtocol,
        adapterDataLoaderAction);
    if (childViewMatcher.isPresent()) {
      targetView = allOf(childViewMatcher.get(), isDescendantOfA(targetView));
    }
    return targetView;
  }

  private Matcher<View> displayingData(
      final Matcher<View> adapterMatcher,
      final Matcher<Object> dataMatcher,
      final AdapterViewProtocol adapterViewProtocol,
      final AdapterDataLoaderAction adapterDataLoaderAction) {
    checkNotNull(adapterMatcher);
    checkNotNull(dataMatcher);
    checkNotNull(adapterViewProtocol);

    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(" displaying data matching: ");
        dataMatcher.describeTo(description);
        description.appendText(" within adapter view matching: ");
        adapterMatcher.describeTo(description);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean matchesSafely(View view) {

        ViewParent parent = view.getParent();

        while (parent != null && !(parent instanceof AdapterView)) {
          parent = parent.getParent();
        }

        if (parent != null && adapterMatcher.matches(parent)) {
          Optional<AdaptedData> data = adapterViewProtocol.getDataRenderedByView(
              (AdapterView<? extends Adapter>) parent, view);
          if (data.isPresent()) {
            return adapterDataLoaderAction.getAdaptedData().opaqueToken.equals(
                data.get().opaqueToken);
          }
        }
        return false;
      }
    };
  }
}
