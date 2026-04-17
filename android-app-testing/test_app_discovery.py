"""
Deprecated discovery entrypoint.

The original Appium-based smoke discovery flow has been replaced by native
Android instrumentation tests that run inside the app module.

Use one of these instead:
  - Android Studio run configuration: "Onboarding Discovery Tests"
  - Gradle: .\gradlew.bat :app:connectedDebugAndroidTest ^
      -Pandroid.testInstrumentationRunnerArguments.class=\
com.tonytrim.fitover40.ui.onboarding.AppDiscoveryTest,\
com.tonytrim.fitover40.ui.onboarding.OnboardingScreenTest
"""

raise SystemExit(
    "test_app_discovery.py is deprecated. Run the native Android tests in "
    "app/src/androidTest instead."
)
