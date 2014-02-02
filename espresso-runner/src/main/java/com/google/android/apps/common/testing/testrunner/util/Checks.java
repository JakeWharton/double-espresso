package com.google.android.apps.common.testing.testrunner.util;

/**
 * Directly depending on com.google.common.base.* can cause build and runtime issues for some
 * applications.
 *
 * To totally avoid these problems we would need to set certain flags on the device prior to
 * installation of the app and test app packages. We can only do this on emulators and rooted
 * hardware. We also need to have control of the test stack to ensure the environment is properly
 * configured before our tests are installed.
 *
 * In order to make GITR usable in any situation where stock ITR is usable we must reluctantly
 * inline these checks from Preconditions.
 */
public final class Checks {

  private Checks() { }

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference,
      String errorMessageTemplate,
      Object... errorMessageArgs) {
    if (reference == null) {
      // If either of these parameters is null, the right thing happens anyway
      throw new NullPointerException(
          format(errorMessageTemplate, errorMessageArgs));
    }
    return reference;
  }

  private static String format(String template, Object... args) {
    template = String.valueOf(template); // null -> "null"

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(
        template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template.substring(templateStart, placeholderStart));
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template.substring(templateStart));

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }
}
