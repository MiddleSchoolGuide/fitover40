package com.tonytrim.fitover40

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.tonytrim.fitover40.data.db.AppDatabase
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import com.tonytrim.fitover40.data.pref.OnboardingPrefs
import com.tonytrim.fitover40.domain.model.TrainingLevel
import com.tonytrim.fitover40.navigation.Screen
import com.tonytrim.fitover40.ui.history.HistoryViewModel
import com.tonytrim.fitover40.ui.onboarding.OnboardingScreen
import com.tonytrim.fitover40.ui.running.RunningScreen
import com.tonytrim.fitover40.ui.running.RunningViewModel
import com.tonytrim.fitover40.ui.settings.SettingsScreen
import com.tonytrim.fitover40.ui.settings.PrivacyPolicyScreen
import com.tonytrim.fitover40.ui.strength.StrengthScreen
import com.tonytrim.fitover40.ui.strength.StrengthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val database = AppDatabase.getDatabase(this)
        val repository = WorkoutRepository(database.workoutDao())
        val onboardingPrefs = OnboardingPrefs(this)

        setContent {
            val hasOnboarded by onboardingPrefs.hasOnboarded.collectAsState(initial = null)
            val savedLevelKey by onboardingPrefs.selectedTrainingLevel.collectAsState(initial = null)
            val scope = rememberCoroutineScope()

            FitOver40Theme {
                if (hasOnboarded == null) {
                    // Loading state if needed
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                } else {
                    MainScreen(
                        repository = repository,
                        hasOnboardedInitially = hasOnboarded == true,
                        initialTrainingLevel = TrainingLevel.fromStorageKey(savedLevelKey),
                        onOnboardingComplete = { level ->
                            scope.launch {
                                onboardingPrefs.saveOnboardingComplete(level.storageKey)
                            }
                        },
                        onClearAllData = {
                            scope.launch {
                                onboardingPrefs.clearAllData()
                                // Room data clearing should also happen here
                                repository.clearAllHistory()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    repository: WorkoutRepository,
    hasOnboardedInitially: Boolean,
    initialTrainingLevel: TrainingLevel?,
    onOnboardingComplete: (TrainingLevel) -> Unit,
    onClearAllData: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var hasOnboarded by rememberSaveable { mutableStateOf(hasOnboardedInitially) }
    var selectedTrainingLevel by rememberSaveable { mutableStateOf(initialTrainingLevel) }

    // State for units and rest time (could be moved to ViewModel or DataStore)
    var useMetricUnits by remember { mutableStateOf(true) }
    var defaultRestSeconds by remember { mutableStateOf(60) }

    if (!hasOnboarded || selectedTrainingLevel == null) {
        OnboardingScreen(
            selectedLevel = selectedTrainingLevel,
            onLevelSelected = { selectedTrainingLevel = it }
        ) { level ->
            hasOnboarded = true
            selectedTrainingLevel = level
            onOnboardingComplete(level)
        }
        return
    }

    val showBottomBar = currentRoute in listOf(
        Screen.History.route,
        Screen.Running.route,
        Screen.Strength.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentRoute == Screen.History.route,
                        onClick = {
                            navController.navigate(Screen.History.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Running") },
                        label = { Text("Running") },
                        selected = currentRoute == Screen.Running.route,
                        onClick = {
                            navController.navigate(Screen.Running.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Strength") },
                        label = { Text("Strength") },
                        selected = currentRoute == Screen.Strength.route,
                        onClick = {
                            navController.navigate(Screen.Strength.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHostWithManualInjection(
                navController = navController,
                repository = repository,
                trainingLevel = selectedTrainingLevel ?: TrainingLevel.BeginnerFirstTimeEver,
                useMetricUnits = useMetricUnits,
                onUnitsToggled = { useMetricUnits = it },
                defaultRestSeconds = defaultRestSeconds,
                onRestSecondsChanged = { defaultRestSeconds = it },
                onClearAllData = {
                    onClearAllData()
                    hasOnboarded = false
                },
                onTrainingLevelChanged = { level ->
                    selectedTrainingLevel = level
                    onOnboardingComplete(level)
                }
            )
        }
    }
}

@Composable
fun NavHostWithManualInjection(
    navController: NavHostController,
    repository: WorkoutRepository,
    trainingLevel: TrainingLevel,
    useMetricUnits: Boolean,
    onUnitsToggled: (Boolean) -> Unit,
    defaultRestSeconds: Int,
    onRestSecondsChanged: (Int) -> Unit,
    onClearAllData: () -> Unit,
    onTrainingLevelChanged: (TrainingLevel) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.History.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.History.route) {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(repository) as T
                }
            }
            com.tonytrim.fitover40.ui.history.HistoryScreen(viewModel(factory = factory))
        }
        composable(Screen.Running.route) {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RunningViewModel(
                        repository,
                        androidx.lifecycle.SavedStateHandle(),
                        trainingLevel
                    ) as T
                }
            }
            RunningScreen(viewModel(key = "running-${trainingLevel.storageKey}", factory = factory))
        }
        composable(Screen.Strength.route) {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StrengthViewModel(repository, trainingLevel) as T
                }
            }
            StrengthScreen(viewModel(key = "strength-${trainingLevel.storageKey}", factory = factory))
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                selectedLevel = trainingLevel,
                onTrainingLevelSelected = onTrainingLevelChanged,
                useMetricUnits = useMetricUnits,
                onUnitsToggled = onUnitsToggled,
                defaultRestSeconds = defaultRestSeconds,
                onRestSecondsChanged = onRestSecondsChanged,
                onClearHistory = onClearAllData,
                onPrivacyPolicyClick = { navController.navigate("privacy_policy") },
                onRequestNotificationPermission = { /* Handle permission request */ }
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun FitOver40Theme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFF89D4A7),
            onPrimary = Color(0xFF09351F),
            primaryContainer = Color(0xFF205131),
            onPrimaryContainer = Color(0xFFA5F1C1),
            secondary = Color(0xFFFFB782),
            onSecondary = Color(0xFF4D2600),
            secondaryContainer = Color(0xFF6E3A00),
            onSecondaryContainer = Color(0xFFFFDCC3),
            tertiary = Color(0xFFB8C4FF),
            onTertiary = Color(0xFF202F61),
            tertiaryContainer = Color(0xFF37467A),
            onTertiaryContainer = Color(0xFFDCE1FF),
            background = Color(0xFF101510),
            onBackground = Color(0xFFE0E4DD),
            surface = Color(0xFF171D18),
            onSurface = Color(0xFFE0E4DD),
            surfaceVariant = Color(0xFF404941),
            onSurfaceVariant = Color(0xFFC0C9BF),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005)
        )
        else -> lightColorScheme(
            primary = Color(0xFF2D6A4F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB6F1CB),
            onPrimaryContainer = Color(0xFF072113),
            secondary = Color(0xFF9C4F19),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFDCC8),
            onSecondaryContainer = Color(0xFF351000),
            tertiary = Color(0xFF465A91),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDCE1FF),
            onTertiaryContainer = Color(0xFF00174A),
            background = Color(0xFFF7FBF6),
            onBackground = Color(0xFF171D18),
            surface = Color(0xFFFCFDF8),
            onSurface = Color(0xFF171D18),
            surfaceVariant = Color(0xFFDCE5DA),
            onSurfaceVariant = Color(0xFF404941),
            error = Color(0xFFBA1A1A),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(36.dp)
        ),
        content = content
    )
}
