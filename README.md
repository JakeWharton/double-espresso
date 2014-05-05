Double Espresso
===============

A pure Gradle port of the [Espresso][1] testing utility for Android!

Note: From android gradle plugin 0.9, use ``androidTestCompile`` for dependencies instead of ``instrumentTestCompile`` (see [plugin migration instructions][6])

No more fumbling with local jars or dependency conflicts. Pull it in with one line:
```groovy
androidTestCompile 'com.jakewharton.espresso:espresso:1.1-r3'
```
Espresso also has an add-on module which provides helpers for the support-v4 library:
```groovy
androidTestCompile 'com.jakewharton.espresso:espresso-support-v4:1.1-r3'
```

Configure the build to use Espresso's custom test runner:
```groovy
defaultConfig {
  testInstrumentationRunner "com.google.android.apps.common.testing.testrunner.GoogleInstrumentationTestRunner"
}
```

The Espresso classes have not been modified from the original version. This exists only as a means
of creating artifacts for native consumption by the new Gradle Android build system. This project
will be immediately deprecated once Google releases a proper version of Espresso that can be
consumed without hassle. A diff of the changes is [available to view][5].

**DO NOT** submit pull requests for features, functionality, or bug fixes. Direct those to
[the original project][1] on Google Code. Only changes to which further the port to Gradle will be
accepted.



Duplicated Dependencies
-----------------------

Due to [a bug][3] in the current Android plugin, you may need to exclude dependencies which are
duplicated in both the app and test app.

For example, if you have a dependency on [Dagger][4] you will need to manually exclude it from
the test dependency for the time being.
```groovy
androidTestCompile('com.jakewharton.espresso:espresso:1.1-r3') {
  exclude group: 'com.squareup.dagger'
}
```

The following are the dependencies of Espresso which may need to be temporarily excluded:
```
com.squareup.dagger:dagger:1.2.1
javax.inject:javax.inject:1
javax.annotation:javax.annotation-api:1.2
com.google.guava:guava:16.0
com.google.code.findbugs:jsr305:1.3.9
org.hamcrest:hamcrest-core:1.1
org.hamcrest:hamcrest-library:1.1
org.hamcrest:hamcrest-integration:1.1
```
and those of the 'support-v4' module:
```
com.android.support:support-v4:19.0.1
```



Notes
-----

The following things are worth noting from the migration:

 *  Module names have been shuffled to be structured more logically.

        OLD                        NEW
        ------------------------   ------------------------
        espresso/lib/              espresso/
        espresso/libtests/         espresso-tester/ (src/instrumentTest)
        espresso/contrib/          espresso-support-v4/
        espresso/contribtests/     espresso-support-v4-tester/ (src/instrumentTest)
        testapp/                   espresso-sample/
        testapp_test/              espresso-sample/ (src/instrumentTest)
        testrunner/                espresso-runner/
        testrunner-runtime/        espresso-runner-runtime/

 * The Maven build used the 'testapp' module for the 'libtests', 'contribtests', and 'testapp_test'
   tests. Since Gradle does not easily facilitate this, specific parts of 'testapp' were moved into
   the `src/main/` folders of the 'espress-tester' and 'espresso-support-v4-tester' modules for
   direct use.
 * Declaring activities inside the instrumentation test for library projects [is not supported][2].
   To work around this, we mirror the Maven technique of having "tester" projects adjacent to the
   libraries. The activities the tests exercise are in `src/main/` with a proper manifest.
 * Dagger was updated to version 1.2.1.
 * Guava was updated to version 16.0.






 [1]: https://code.google.com/p/android-test-kit/
 [2]: http://b.android.com/57819
 [3]: http://b.android.com/65445
 [4]: http://square.github.io/dagger
 [5]: https://github.com/JakeWharton/double-espresso/compare/master...gradle
 [6]: http://tools.android.com/tech-docs/new-build-system/migrating_to_09
