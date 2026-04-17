I have a completed Android fitness app called FitOver40 built in Kotlin with
Jetpack Compose, MVVM, and Room. Prepare this app for Google Play Store
submission by implementing everything in the checklist below.

=== APP IDENTITY ===
- App name: FitOver40
- Package name: com.tonytrim.fitover40
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
- Contact email placeholder: furbert.trim@gmail.com
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
Strength & interval running for 40+ fitness. Private, offline, no ads.

FULL DESCRIPTION (max 4000 chars), include:
### FitOver40: Strength & Stamina for the Prime of Your Life

**Reclaiming your fitness shouldn't mean risking your joints.**

FitOver40 is the no-nonsense training companion built specifically for the 40+ athlete. We understand that your goals have shifted: you want to maintain muscle mass, build cardiovascular endurance, and stay mobile—all without the high-impact "grind" of generic fitness apps.

Whether you're returning to exercise after a break or looking to optimize your current routine for longevity, FitOver40 provides a structured, science-backed approach to fitness in your 40s, 50s, and beyond.

---
### 🏃‍♂️ INTERVAL RUNNING (OUTDOOR & TREADMILL)
Build your engine without the wear and tear. Our interval running plans focus on timed work-to-rest ratios, allowing you to build stamina progressively.
- **Smart Tracking**: Choose between Outdoor (GPS) or Treadmill mode.
- **Bluetooth FTMS Support**: Connect directly to compatible Bluetooth treadmills for real-time speed and distance syncing.
- **Motion Sensor Fallback**: No Bluetooth? No problem. The app uses your phone's motion sensors to track steps and estimated distance.
- **Haptic Guidance**: Feel the switch between RUN and WALK phases with haptic vibrations, so you can keep your eyes on the path or the TV.

---
### 🏋️‍♂️ STRENGTH TRAINING (JOINT-FRIENDLY)
Muscle is the "currency of longevity." FitOver40 focuses on functional movements that build strength while protecting your back, knees, and shoulders.
- **Built-in Guidance**: Every exercise comes with clear "Get Set" instructions and "Form Cues" to ensure safe execution.
- **Regressions Included**: If a movement feels too hard, we provide an "Easier Option" right on the screen.
- **Movement Previews**: Visualize the movement with clear, integrated illustrations before you start.
- **Rest & Recovery**: Integrated rest timers remind you to focus on breathing and form between sets.
- **Progressive Overload**: Log your weight (kg/lb) and reps to see your strength grow over time.

---
### 🔒 PRIVACY FIRST
Your data is personal. We believe it should stay that way.
- **No Account Required**: Start training in seconds. We don't need your email, your phone number, or your social media.
- **100% Offline & Local**: Your workout history, plans, and metrics are stored strictly on your device.
- **No Tracking, No Ads**: We don't use third-party analytics, tracking pixels, or annoying ad banners. Your focus stays on your workout.

---
### ✅ WHY FITOVER40?
- **Designed for Mature Athletes**: Realistic goals, joint-friendly programming, and a focus on longevity.
- **Simplicity & Speed**: No fluff, no social feeds, no distracting videos. Just training.
- **Comprehensive Tracking**: View your history and progress at a glance.
- **Health-First Approach**: Includes a mandatory health disclaimer because your safety is paramount.

**Note**: Before starting any new exercise program, consult your physician or healthcare provider, especially if you have pre-existing medical conditions. This app does not provide medical advice.

**Train smart. Live strong. Reclaim your prime with FitOver40.**

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
