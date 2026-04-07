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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.tonytrim.fitover40.BuildConfig
import com.tonytrim.fitover40.data.account.AccountApi
import com.tonytrim.fitover40.data.account.AccountRepository
import com.tonytrim.fitover40.data.auth.AuthApi
import com.tonytrim.fitover40.data.auth.AuthRepository
import com.tonytrim.fitover40.data.db.AppDatabase
import com.tonytrim.fitover40.data.pref.AuthPrefs
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import com.tonytrim.fitover40.data.pref.OnboardingPrefs
import com.tonytrim.fitover40.data.sync.WorkoutSyncApi
import com.tonytrim.fitover40.data.sync.WorkoutSyncRepository
import com.tonytrim.fitover40.domain.model.TrainingLevel
import com.tonytrim.fitover40.navigation.Screen
import com.tonytrim.fitover40.ui.auth.AuthScreen
import com.tonytrim.fitover40.ui.auth.AuthViewModel
import com.tonytrim.fitover40.ui.history.HistoryViewModel
import com.tonytrim.fitover40.ui.onboarding.OnboardingScreen
import com.tonytrim.fitover40.ui.running.RunningScreen
import com.tonytrim.fitover40.ui.running.RunningViewModel
import com.tonytrim.fitover40.ui.settings.AccountViewModel
import com.tonytrim.fitover40.ui.settings.SettingsScreen
import com.tonytrim.fitover40.ui.settings.PrivacyPolicyScreen
import com.tonytrim.fitover40.ui.settings.WorkoutSyncViewModel
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
        val authRepository = AuthRepository(
            authApi = AuthApi(BuildConfig.AUTH_BASE_URL),
            authPrefs = AuthPrefs(this)
        )
        val accountRepository = AccountRepository(
            accountApi = AccountApi(BuildConfig.AUTH_BASE_URL),
            authRepository = authRepository
        )
        val workoutSyncRepository = WorkoutSyncRepository(
            syncApi = WorkoutSyncApi(BuildConfig.AUTH_BASE_URL),
            workoutRepository = repository,
            authRepository = authRepository
        )

        setContent {
            val hasOnboarded by onboardingPrefs.hasOnboarded.collectAsState(initial = null)
            val savedLevelKey by onboardingPrefs.selectedTrainingLevel.collectAsState(initial = null)
            val authSession by authRepository.session.collectAsState(initial = null)
            val scope = rememberCoroutineScope()
            var authReady by remember { mutableStateOf(false) }

            LaunchedEffect(authSession?.refreshToken, authSession?.expiresAtEpochSeconds, authSession?.accessToken) {
                authReady = false
                if (authSession != null) {
                    authRepository.ensureValidSession()
                }
                authReady = true
            }

            FitOver40Theme {
                if (hasOnboarded == null || !authReady) {
                    // Loading state if needed
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                } else if (authSession == null) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AuthViewModel(authRepository) as T
                        }
                    }
                    AuthScreen(viewModel(factory = factory)) {}
                } else {
                    MainScreen(
                        repository = repository,
                        accountRepository = accountRepository,
                        workoutSyncRepository = workoutSyncRepository,
                        signedInEmail = authSession?.email,
                        signedInName = authSession?.displayName,
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
                        },
                        onSignOut = {
                            scope.launch {
                                authRepository.signOut()
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
    accountRepository: AccountRepository,
    workoutSyncRepository: WorkoutSyncRepository,
    signedInEmail: String?,
    signedInName: String?,
    hasOnboardedInitially: Boolean,
    initialTrainingLevel: TrainingLevel?,
    onOnboardingComplete: (TrainingLevel) -> Unit,
    onClearAllData: () -> Unit,
    onSignOut: () -> Unit
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
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentRoute == Screen.History.route,
                        onClick = {
                            navController.navigate(Screen.History.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Running") },
                        label = { Text("Running") },
                        selected = currentRoute == Screen.Running.route,
                        onClick = {
                            navController.navigate(Screen.Running.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Strength") },
                        label = { Text("Strength") },
                        selected = currentRoute == Screen.Strength.route,
                        onClick = {
                            navController.navigate(Screen.Strength.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                accountRepository = accountRepository,
                workoutSyncRepository = workoutSyncRepository,
                signedInEmail = signedInEmail,
                signedInName = signedInName,
                trainingLevel = selectedTrainingLevel ?: TrainingLevel.BeginnerFirstTimeEver,
                useMetricUnits = useMetricUnits,
                onUnitsToggled = { useMetricUnits = it },
                defaultRestSeconds = defaultRestSeconds,
                onRestSecondsChanged = { defaultRestSeconds = it },
                onClearAllData = {
                    onClearAllData()
                    hasOnboarded = false
                },
                onSignOut = onSignOut,
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
    accountRepository: AccountRepository,
    workoutSyncRepository: WorkoutSyncRepository,
    signedInEmail: String?,
    signedInName: String?,
    trainingLevel: TrainingLevel,
    useMetricUnits: Boolean,
    onUnitsToggled: (Boolean) -> Unit,
    defaultRestSeconds: Int,
    onRestSecondsChanged: (Int) -> Unit,
    onClearAllData: () -> Unit,
    onSignOut: () -> Unit,
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
            val accountFactory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AccountViewModel(accountRepository) as T
                }
            }
            val syncFactory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorkoutSyncViewModel(workoutSyncRepository) as T
                }
            }
            val accountViewModel: AccountViewModel = viewModel(factory = accountFactory)
            val workoutSyncViewModel: WorkoutSyncViewModel = viewModel(factory = syncFactory)
            val accountUiState by accountViewModel.uiState.collectAsState()
            val workoutSyncUiState by workoutSyncViewModel.uiState.collectAsState()
            SettingsScreen(
                accountUiState = accountUiState,
                onRefreshAccount = accountViewModel::refresh,
                workoutSyncUiState = workoutSyncUiState,
                onSyncWorkouts = workoutSyncViewModel::syncNow,
                selectedLevel = trainingLevel,
                signedInEmail = signedInEmail,
                signedInName = signedInName,
                onTrainingLevelSelected = onTrainingLevelChanged,
                useMetricUnits = useMetricUnits,
                onUnitsToggled = onUnitsToggled,
                defaultRestSeconds = defaultRestSeconds,
                onRestSecondsChanged = onRestSecondsChanged,
                onClearHistory = onClearAllData,
                onSignOut = onSignOut,
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
    val colorScheme: ColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF0A84FF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF16395F),
            onPrimaryContainer = Color(0xFFD6E9FF),
            secondary = Color(0xFF30D158),
            onSecondary = Color(0xFF03210C),
            secondaryContainer = Color(0xFF153A1E),
            onSecondaryContainer = Color(0xFFBAF5C8),
            tertiary = Color(0xFFFF9F0A),
            onTertiary = Color(0xFF331C00),
            tertiaryContainer = Color(0xFF4D2E00),
            onTertiaryContainer = Color(0xFFFFDFB3),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF1C1C1E),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF2C2C2E),
            onSurfaceVariant = Color(0xFFAEAEB2),
            outline = Color(0xFF545458),
            outlineVariant = Color(0xFF3A3A3C),
            error = Color(0xFFFF453A),
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF007AFF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD6E9FF),
            onPrimaryContainer = Color(0xFF002C63),
            secondary = Color(0xFF34C759),
            onSecondary = Color(0xFF06210D),
            secondaryContainer = Color(0xFFDDF8E4),
            onSecondaryContainer = Color(0xFF0E381A),
            tertiary = Color(0xFFFF9500),
            onTertiary = Color(0xFF3D2200),
            tertiaryContainer = Color(0xFFFFE0B8),
            onTertiaryContainer = Color(0xFF4F2A00),
            background = Color(0xFFF2F2F7),
            onBackground = Color(0xFF000000),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF000000),
            surfaceVariant = Color(0xFFF2F2F7),
            onSurfaceVariant = Color(0xFF636366),
            outline = Color(0xFF8E8E93),
            outlineVariant = Color(0xFFCED0D6),
            error = Color(0xFFFF3B30),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            displayLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 41.sp
            ),
            displayMedium = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 34.sp
            ),
            headlineMedium = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 34.sp
            ),
            headlineSmall = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp
            ),
            titleLarge = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 25.sp
            ),
            titleMedium = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 22.sp
            ),
            titleSmall = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 20.sp
            ),
            bodyLarge = TextStyle(
                fontSize = 17.sp,
                lineHeight = 22.sp
            ),
            bodyMedium = TextStyle(
                fontSize = 16.sp,
                lineHeight = 21.sp
            ),
            bodySmall = TextStyle(
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            labelLarge = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                lineHeight = 22.sp
            ),
            labelMedium = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(10.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(22.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
