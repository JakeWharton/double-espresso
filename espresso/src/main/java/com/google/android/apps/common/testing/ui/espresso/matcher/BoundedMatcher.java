package com.google.android.apps.common.testing.ui.espresso.matcher;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.hamcrest.BaseMatcher;

/**
 * Some matcher sugar that lets you create a matcher for a given type
 * but only process items of a specific subtype of that matcher.
 *
 * @param <T> The desired type of the Matcher.
 * @param <S> the subtype of T that your matcher applies safely to.
 */
public abstract class BoundedMatcher<T, S extends T> extends BaseMatcher<T> {

  private final Class<?> expectedType;
  private final Class<?>[] interfaceTypes;

  public BoundedMatcher(Class<? extends S> expectedType) {
    this.expectedType = checkNotNull(expectedType);
    this.interfaceTypes = new Class[0];
  }

  public BoundedMatcher(Class<?> expectedType, Class<?> interfaceType1,
      Class<?>... otherInterfaces) {
    this.expectedType = checkNotNull(expectedType);
    checkNotNull(otherInterfaces);
    int interfaceCount = otherInterfaces.length + 1;
    this.interfaceTypes = new Class[interfaceCount];

    interfaceTypes[0] = checkNotNull(interfaceType1);
    checkArgument(interfaceType1.isInterface());
    int interfaceTypeIdx = 1;
    for (Class<?> intfType : otherInterfaces) {
      interfaceTypes[interfaceTypeIdx] = checkNotNull(intfType);
      checkArgument(intfType.isInterface());
      interfaceTypeIdx++;
    }
  }

  protected abstract boolean matchesSafely(S item);

  @Override
  @SuppressWarnings({"unchecked"})
  public final boolean matches(Object item) {
    if (item == null) {
      return false;
    }

    if (expectedType.isInstance(item)) {
      for (Class<?> intfType : interfaceTypes) {
        if (!intfType.isInstance(item)) {
          return false;
        }
      }
      return matchesSafely((S) item);
    }
    return false;
  }
}
