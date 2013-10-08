package com.google.android.apps.common.testing.ui.espresso.matcher;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasDescendant;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasImeAction;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isChecked;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isClickable;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isEnabled;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isFocusable;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isNotChecked;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isRoot;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.supportsInputMethods;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withChild;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagKey;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.Visibility;
import com.google.android.apps.common.testing.ui.espresso.tests.R;

import android.test.InstrumentationTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Unit tests for {@link ViewMatchers}.
 */
public class ViewMatchersTest extends InstrumentationTestCase {
  public void testIsAssignableFrom_notAnInstance() {
    View v = new View(getInstrumentation().getTargetContext());
    assertFalse(isAssignableFrom(Spinner.class).matches(v));
  }

  public void testIsAssignableFrom_plainView() {
    View v = new View(getInstrumentation().getTargetContext());
    assertTrue(isAssignableFrom(View.class).matches(v));
  }

  public void testIsAssignableFrom_superclass() {
    View v = new RadioButton(getInstrumentation().getTargetContext());
    assertTrue(isAssignableFrom(Button.class).matches(v));
  }

  @SuppressWarnings("cast")
  public void testWithContentDescriptionCharSequence() {
    View view = new View(getInstrumentation().getTargetContext());
    view.setContentDescription(null);
    assertTrue(withContentDescription(Matchers.<CharSequence>nullValue()).matches(view));
    CharSequence testText = "test text!";
    view.setContentDescription(testText);
    assertTrue(withContentDescription(is(testText)).matches(view));
    assertFalse(withContentDescription(is((CharSequence) "blah")).matches(view));
    assertFalse(withContentDescription(is((CharSequence) "")).matches(view));
  }

  public void testWithContentDescriptionNull() {
    try {
      withContentDescription((Matcher<CharSequence>) null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
  }

  public void testHasContentDescription() {
    View view = new View(getInstrumentation().getTargetContext());
    view.setContentDescription(null);
    assertFalse(hasContentDescription().matches(view));
    CharSequence testText = "test text!";
    view.setContentDescription(testText);
    assertTrue(hasContentDescription().matches(view));
  }

  public void testWithContentDescriptionString() {
    View view = new View(getInstrumentation().getTargetContext());
    view.setContentDescription(null);
    assertTrue(withContentDescription(Matchers.<String>nullValue()).matches(view));
    String testText = "test text!";
    view.setContentDescription(testText);
    assertTrue(withContentDescription(is(testText)).matches(view));
    assertFalse(withContentDescription(is("blah")).matches(view));
    assertFalse(withContentDescription(is("")).matches(view));
  }

  public void testWithId() {
    View view = new View(getInstrumentation().getTargetContext());
    view.setId(R.id.testId1);
    assertTrue(withId(is(R.id.testId1)).matches(view));
    assertFalse(withId(is(R.id.testId2)).matches(view));
    assertFalse(withId(is(1234)).matches(view));
  }

  public void testWithTagNull() {
    try {
      withTagKey(0, null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }

    try {
      withTagValue(null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
  }

  public void testWithTagObject() {
    View view = new View(getInstrumentation().getTargetContext());
    view.setTag(null);
    assertTrue(withTagValue(Matchers.<Object>nullValue()).matches(view));
    String testObjectText = "test text!";
    view.setTag(testObjectText);
    assertFalse(withTagKey(R.id.testId1).matches(view));
    assertTrue(withTagValue(is((Object) testObjectText)).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
    assertFalse(withTagValue(is((Object) "")).matches(view));
  }

  public void testWithTagKey() {
    View view = new View(getInstrumentation().getTargetContext());
    assertFalse(withTagKey(R.id.testId1).matches(view));
    view.setTag(R.id.testId1, "blah");
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
    assertTrue(withTagKey(R.id.testId1).matches(view));
    assertFalse(withTagKey(R.id.testId2).matches(view));
    assertFalse(withTagKey(R.id.testId3).matches(view));
    assertFalse(withTagKey(65535).matches(view));

    view.setTag(R.id.testId2, "blah2");
    assertTrue(withTagKey(R.id.testId1).matches(view));
    assertTrue(withTagKey(R.id.testId2).matches(view));
    assertFalse(withTagKey(R.id.testId3).matches(view));
    assertFalse(withTagKey(65535).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
  }

  public void testWithTagKeyObject() {
    View view = new View(getInstrumentation().getTargetContext());
    String testObjectText1 = "test text1!";
    String testObjectText2 = "test text2!";
    assertFalse(withTagKey(R.id.testId1, is((Object) testObjectText1)).matches(view));
    view.setTag(R.id.testId1, testObjectText1);
    assertTrue(withTagKey(R.id.testId1, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId1, is((Object) testObjectText2)).matches(view));
    assertFalse(withTagKey(R.id.testId2, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId3, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(65535, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));

    view.setTag(R.id.testId2, testObjectText2);
    assertTrue(withTagKey(R.id.testId1, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId1, is((Object) testObjectText2)).matches(view));
    assertTrue(withTagKey(R.id.testId2, is((Object) testObjectText2)).matches(view));
    assertFalse(withTagKey(R.id.testId2, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId3, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(65535, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
  }

  public void testWithTextNull() {
    try {
      withText((Matcher<String>) null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
  }

  public void testCheckBoxMatchers() {
    assertFalse(isChecked().matches(new Spinner(getInstrumentation().getTargetContext())));
    assertFalse(isNotChecked().matches(new Spinner(getInstrumentation().getTargetContext())));

    CheckBox checkBox = new CheckBox(getInstrumentation().getTargetContext());
    checkBox.setChecked(true);
    assertTrue(isChecked().matches(checkBox));
    assertFalse(isNotChecked().matches(checkBox));

    checkBox.setChecked(false);
    assertFalse(isChecked().matches(checkBox));
    assertTrue(isNotChecked().matches(checkBox));

    RadioButton radioButton = new RadioButton(getInstrumentation().getTargetContext());
    radioButton.setChecked(false);
    assertFalse(isChecked().matches(radioButton));
    assertTrue(isNotChecked().matches(radioButton));

    radioButton.setChecked(true);
    assertTrue(isChecked().matches(radioButton));
    assertFalse(isNotChecked().matches(radioButton));

    CheckedTextView checkedText = new CheckedTextView(getInstrumentation().getTargetContext());
    checkedText.setChecked(false);
    assertFalse(isChecked().matches(checkedText));
    assertTrue(isNotChecked().matches(checkedText));

    checkedText.setChecked(true);
    assertTrue(isChecked().matches(checkedText));
    assertFalse(isNotChecked().matches(checkedText));

    Checkable checkable = new Checkable() {
      @Override
      public boolean isChecked() { return true; }
      @Override
      public void setChecked(boolean ignored) {}
      @Override
      public void toggle() {}
    };

    assertFalse(isChecked().matches(checkable));
    assertFalse(isNotChecked().matches(checkable));
  }

  public void testWithTextString() {
    TextView textView = new TextView(getInstrumentation().getTargetContext());
    textView.setText(null);
    assertTrue(withText(is("")).matches(textView));
    String testText = "test text!";
    textView.setText(testText);
    assertTrue(withText(is(testText)).matches(textView));
    assertFalse(withText(is("blah")).matches(textView));
    assertFalse(withText(is("")).matches(textView));
  }

  public void testHasDescendant() {
    View v = new TextView(getInstrumentation().getTargetContext());
    ViewGroup parent = new RelativeLayout(getInstrumentation().getTargetContext());
    ViewGroup grany = new ScrollView(getInstrumentation().getTargetContext());
    grany.addView(parent);
    parent.addView(v);
    assertTrue(hasDescendant(isAssignableFrom(TextView.class)).matches(grany));
    assertTrue(hasDescendant(isAssignableFrom(TextView.class)).matches(parent));
    assertFalse(hasDescendant(isAssignableFrom(ScrollView.class)).matches(parent));
    assertFalse(hasDescendant(isAssignableFrom(TextView.class)).matches(v));
  }

  public void testIsDescendantOfA() {
    View v = new TextView(getInstrumentation().getTargetContext());
    ViewGroup parent = new RelativeLayout(getInstrumentation().getTargetContext());
    ViewGroup grany = new ScrollView(getInstrumentation().getTargetContext());
    grany.addView(parent);
    parent.addView(v);
    assertTrue(isDescendantOfA(isAssignableFrom(RelativeLayout.class)).matches(v));
    assertTrue(isDescendantOfA(isAssignableFrom(ScrollView.class)).matches(v));
    assertFalse(isDescendantOfA(isAssignableFrom(LinearLayout.class)).matches(v));
  }

  public void testIsVisible() {
    View visible = new View(getInstrumentation().getTargetContext());
    visible.setVisibility(View.VISIBLE);
    View invisible = new View(getInstrumentation().getTargetContext());
    invisible.setVisibility(View.INVISIBLE);
    assertTrue(withEffectiveVisibility(Visibility.VISIBLE).matches(visible));
    assertFalse(withEffectiveVisibility(Visibility.VISIBLE).matches(invisible));

    // Make the visible view invisible by giving it an invisible parent.
    ViewGroup parent = new RelativeLayout(getInstrumentation().getTargetContext());
    parent.addView(visible);
    parent.setVisibility(View.INVISIBLE);
    assertFalse(withEffectiveVisibility(Visibility.VISIBLE).matches(visible));
  }

  public void testIsInvisible() {
    View visible = new View(getInstrumentation().getTargetContext());
    visible.setVisibility(View.VISIBLE);
    View invisible = new View(getInstrumentation().getTargetContext());
    invisible.setVisibility(View.INVISIBLE);
    assertFalse(withEffectiveVisibility(Visibility.INVISIBLE).matches(visible));
    assertTrue(withEffectiveVisibility(Visibility.INVISIBLE).matches(invisible));

    // Make the visible view invisible by giving it an invisible parent.
    ViewGroup parent = new RelativeLayout(getInstrumentation().getTargetContext());
    parent.addView(visible);
    parent.setVisibility(View.INVISIBLE);
    assertTrue(withEffectiveVisibility(Visibility.INVISIBLE).matches(visible));
  }

  public void testIsGone() {
    View gone = new View(getInstrumentation().getTargetContext());
    gone.setVisibility(View.GONE);
    View visible = new View(getInstrumentation().getTargetContext());
    visible.setVisibility(View.VISIBLE);
    assertFalse(withEffectiveVisibility(Visibility.GONE).matches(visible));
    assertTrue(withEffectiveVisibility(Visibility.GONE).matches(gone));

    // Make the gone view gone by giving it a gone parent.
    ViewGroup parent = new RelativeLayout(getInstrumentation().getTargetContext());
    parent.addView(visible);
    parent.setVisibility(View.GONE);
    assertTrue(withEffectiveVisibility(Visibility.GONE).matches(visible));
  }

  public void testIsClickable() {
    View clickable = new View(getInstrumentation().getTargetContext());
    clickable.setClickable(true);
    View notClickable = new View(getInstrumentation().getTargetContext());
    notClickable.setClickable(false);
    assertTrue(isClickable().matches(clickable));
    assertFalse(isClickable().matches(notClickable));
  }

  public void testIsEnabled() {
    View enabled = new View(getInstrumentation().getTargetContext());
    enabled.setEnabled(true);
    View notEnabled = new View(getInstrumentation().getTargetContext());
    notEnabled.setEnabled(false);
    assertTrue(isEnabled().matches(enabled));
    assertFalse(isEnabled().matches(notEnabled));
  }

  public void testIsFocusable() {
    View focusable = new View(getInstrumentation().getTargetContext());
    focusable.setFocusable(true);
    View notFocusable = new View(getInstrumentation().getTargetContext());
    notFocusable.setFocusable(false);
    assertTrue(isFocusable().matches(focusable));
    assertFalse(isFocusable().matches(notFocusable));
  }

  public void testWithTextResourceId() {
    TextView textView = new TextView(getInstrumentation().getTargetContext());
    textView.setText(R.string.something);
    assertTrue(withText(R.string.something).matches(textView));
    assertFalse(withText(R.string.other_string).matches(textView));
  }

  public void testWithParent() {
    View view1 = new TextView(getInstrumentation().getTargetContext());
    View view2 = new TextView(getInstrumentation().getTargetContext());
    View view3 = new TextView(getInstrumentation().getTargetContext());
    ViewGroup tiptop = new RelativeLayout(getInstrumentation().getTargetContext());
    ViewGroup secondLevel = new RelativeLayout(getInstrumentation().getTargetContext());
    secondLevel.addView(view2);
    secondLevel.addView(view3);
    tiptop.addView(secondLevel);
    tiptop.addView(view1);
    assertTrue(withParent(is((View) tiptop)).matches(view1));
    assertTrue(withParent(is((View) tiptop)).matches(secondLevel));
    assertFalse(withParent(is((View) tiptop)).matches(view2));
    assertFalse(withParent(is((View) tiptop)).matches(view3));
    assertFalse(withParent(is((View) secondLevel)).matches(view1));

    assertTrue(withParent(is((View) secondLevel)).matches(view2));
    assertTrue(withParent(is((View) secondLevel)).matches(view3));

    assertFalse(withParent(is(view3)).matches(view3));
  }

  public void testWithChild() {
    View view1 = new TextView(getInstrumentation().getTargetContext());
    View view2 = new TextView(getInstrumentation().getTargetContext());
    View view3 = new TextView(getInstrumentation().getTargetContext());
    ViewGroup tiptop = new RelativeLayout(getInstrumentation().getTargetContext());
    ViewGroup secondLevel = new RelativeLayout(getInstrumentation().getTargetContext());
    secondLevel.addView(view2);
    secondLevel.addView(view3);
    tiptop.addView(secondLevel);
    tiptop.addView(view1);
    assertTrue(withChild(is(view1)).matches(tiptop));
    assertTrue(withChild(is((View) secondLevel)).matches(tiptop));
    assertFalse(withChild(is((View) tiptop)).matches(view1));
    assertFalse(withChild(is(view2)).matches(tiptop));
    assertFalse(withChild(is(view1)).matches(secondLevel));

    assertTrue(withChild(is(view2)).matches(secondLevel));

    assertFalse(withChild(is(view3)).matches(view3));
  }

  public void testIsRootView() {
    ViewGroup rootView = new ViewGroup(getInstrumentation().getTargetContext()) {
      @Override
      protected void onLayout(boolean changed, int l, int t, int r, int b) {
      }
    };

    View view = new View(getInstrumentation().getTargetContext());
    rootView.addView(view);

    assertTrue(isRoot().matches(rootView));
    assertFalse(isRoot().matches(view));
  }

  public void testHasSibling() {
    TextView v1 = new TextView(getInstrumentation().getTargetContext());
    v1.setText("Bill Odama");
    Button v2 = new Button(getInstrumentation().getTargetContext());
    View v3 = new View(getInstrumentation().getTargetContext());
    ViewGroup parent = new LinearLayout(getInstrumentation().getTargetContext());
    parent.addView(v1);
    parent.addView(v2);
    parent.addView(v3);
    assertTrue(hasSibling(withText("Bill Odama")).matches(v2));
    assertFalse(hasSibling(is(v3)).matches(parent));
  }

  public void testHasImeAction() {
    EditText editText = new EditText(getInstrumentation().getTargetContext());
    assertFalse(hasImeAction(EditorInfo.IME_ACTION_GO).matches(editText));
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    assertFalse(hasImeAction(EditorInfo.IME_ACTION_GO).matches(editText));
    assertTrue(hasImeAction(EditorInfo.IME_ACTION_NEXT).matches(editText));
  }

  public void testHasImeActionNoInputConnection() {
    Button button = new Button(getInstrumentation().getTargetContext());
    assertFalse(hasImeAction(0).matches(button));
  }

  public void testSupportsInputMethods() {
    Button button = new Button(getInstrumentation().getTargetContext());
    EditText editText = new EditText(getInstrumentation().getTargetContext());
    assertFalse(supportsInputMethods().matches(button));
    assertTrue(supportsInputMethods().matches(editText));
  }
}
