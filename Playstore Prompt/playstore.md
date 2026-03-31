I have a completed Android fitness app called FitOver40 built in Kotlin with
Jetpack Compose, MVVM, and Room. Prepare this app for Google Play Store
submission by implementing everything in the checklist below.

=== APP IDENTITY ===
- App name: FitOver40
- Package name: com.yourname.fitover40
- Version: 1.0.0 (versionCode: 1, versionName: "1.0.0")
- Target audience: Adults 40 and older
- Category: Health & Fitness
- Content rating: Everyone

=== 1. BUILD CONFIGURATION ===
Update app/build.gradle.kts for a production-ready release build:

- Set versionCode = 1 and versionName = "1.0.0"
- Enable R8 full mode minification for release builds
- Enable resource shrinking (shrinkResources = true)
- Configure signing block to read keystore from a local.properties file
  (never hardcode passwords in source):
  storeFile, storePassword, keyAlias, keyPassword
- Set buildTypes release block:
  isMinifyEnabled = true
  isShrinkResources = true
  proguardFiles = getDefaultProguardFile("proguard-android-optimize.txt")
  + "proguard-rules.pro"
- Add this to proguard-rules.pro:
  Keep all Room entities and DAOs
  Keep all data classes used by Room
  Keep Kotlin Serialization classes if used
  Keep ViewModel subclasses

=== 2. SIGNING KEYSTORE SETUP ===
Generate the following instructions and template files:

- Step-by-step terminal command to generate a keystore using keytool:
  keytool -genkey -v -keystore fitover40.jks ...
  (fill in all parameters: validity 25 years, key size 2048,
  algorithm RSA)
- A local.properties template showing where to add keystore credentials
- A .gitignore entry that excludes:
  local.properties
  *.jks
  *.keystore
  (CRITICAL: these must never be committed to source control)

=== 3. APP ICON ===
Generate instructions and code for the complete icon set:

- Create an adaptive icon using Android Studio's Image Asset tool:
  Foreground layer: a bold running figure + dumbbell icon
  Background layer: solid brand color (suggest a strong teal #00897B
  or deep blue #1565C0 — good contrast, energetic but mature)
- Required output sizes to verify exist in res/mipmap-*:
  mipmap-mdpi, mipmap-hdpi, mipmap-xhdpi, mipmap-xxhdpi, mipmap-xxxhdpi
- ic_launcher.png (48dp grid) and ic_launcher_round.png
- Play Store icon spec: 512x512 PNG, no alpha, under 1MB
  (provide ImageMagick or Android Studio export instructions)
- Add the ic_launcher reference to AndroidManifest.xml:
  android:icon="@mipmap/ic_launcher"
  android:roundIcon="@mipmap/ic_launcher_round"

=== 4. SPLASH SCREEN ===
Implement the modern Android 12+ Splash Screen API:

- Add dependency: androidx.core:core-splashscreen:1.0.1
- Create res/values/themes.xml with Theme.FitOver40.Starting:
  windowSplashScreenBackground = brand color
  windowSplashScreenAnimatedIcon = app icon
  postSplashScreenTheme = Theme.FitOver40
- Update AndroidManifest.xml to use the splash theme on MainActivity
- Add installSplashScreen() call at the top of MainActivity.onCreate()

=== 5. PERMISSIONS AUDIT ===
Review AndroidManifest.xml and keep ONLY what is needed:

Required permissions (add these):
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

For each permission include a comment explaining WHY it is needed.
Remove any permissions not explicitly used.
Add uses-feature tags:
<uses-feature android:name="android.hardware.touchscreen"
android:required="true"/>

=== 6. FOREGROUND SERVICE FOR WORKOUT TIMER ===
The interval timer must keep running when the screen is off or app
is backgrounded. Implement a WorkoutTimerService:

- Create WorkoutTimerService : Service() with:
  START_STICKY return in onStartCommand
  Persistent notification with workout phase + time remaining
  Notification channel "Workout Timer" (IMPORTANCE_LOW, no sound)
  PendingIntent back to MainActivity on notification tap
  Binder pattern to allow ViewModel to bind and receive timer ticks
- Register in AndroidManifest.xml with:
  android:foregroundServiceType="health"
- Create NotificationCompat.Builder notification:
  Small icon, ongoing = true, no sound
  Action buttons: Pause and Stop

=== 7. PRIVACY POLICY ===
Generate a complete plain-English privacy policy text covering:

- What data is collected: workout history stored LOCALLY only on device
- What is NOT collected: no personal info, no account required,
  no data sent to servers, no analytics, no ads
- Data deletion: user can clear all data from within the app
  (add a "Clear All Data" option in Settings screen)
- Contact email placeholder: privacy@yourname.com
- Effective date: [today's date]

Also create a PrivacyPolicyScreen.kt Composable that:
- Displays the policy text in a scrollable LazyColumn
- Is accessible from a Settings or About screen
- Can be linked to from the Play Store listing

=== 8. ONBOARDING & HEALTH DISCLAIMER ===
Create an OnboardingScreen.kt shown only on first app launch
(use DataStore Preferences to track if seen):

Screen 1 — Welcome:
"Welcome to FitOver40"
Brief 2-sentence app description

Screen 2 — Health Disclaimer (REQUIRED for fitness apps):
Display this text prominently:
"Before starting any new exercise program, consult your physician
or healthcare provider, especially if you have any pre-existing
medical conditions. This app does not provide medical advice."
Require user to tap "I Understand" to proceed (log acceptance
with timestamp in DataStore)

Screen 3 — Quick feature overview (3 cards: Run, Lift, Track)

Add DataStore Preferences dependency:
androidx.datastore:datastore-preferences:1.0.0

=== 9. SETTINGS SCREEN ===
Create a SettingsScreen.kt with these options:

- Units toggle: Miles / Kilometers
- Default rest time between strength sets (seconds)
- Notification permission request button (Android 13+)
- "Clear All Workout History" with confirmation dialog
- "Privacy Policy" link (navigates to PrivacyPolicyScreen)
- App version display (read from BuildConfig.VERSION_NAME)

=== 10. RELEASE CHECKLIST CODE ===
Add a debug-only screen or log output that verifies:

- [ ] No hardcoded test data left in Room prepopulation
- [ ] No Log.d() calls in release (configure ProGuard to strip them)
- [ ] No TODO comments in production-critical paths
- [ ] All string literals in res/values/strings.xml (no hardcoded
  UI strings in Composables)
- [ ] All colors in res/values/colors.xml or MaterialTheme tokens

=== 11. PLAY STORE LISTING COPY ===
Generate the following text assets for the Play Store listing:

SHORT DESCRIPTION (max 80 chars):
Interval running and strength training designed for 40+ fitness

FULL DESCRIPTION (max 4000 chars), include:
- Hook paragraph about fitness after 40
- Feature bullets for Running, Weight Training, History/Tracking
- Safety-first messaging (consult your doctor)
- Offline/no-account/no-ads selling points
- Call to action

KEYWORD LIST (for ASO - App Store Optimization):
Suggest 10 high-value search terms for the Health & Fitness category
relevant to this app

WHAT'S NEW (for version 1.0.0):
"Initial release — interval running plans, weight training tracker,
and workout history for the 40+ athlete."

=== CONSTRAINTS ===
- All secrets (keystore passwords) must use local.properties,
  never hardcoded
- ProGuard rules must not break Room, Compose, or Navigation
- Foreground service notification must target Android 8.0+
  (API 26) notification channels
- Onboarding must only show once; use DataStore not SharedPreferences
- Health disclaimer acceptance must be logged with a timestamp