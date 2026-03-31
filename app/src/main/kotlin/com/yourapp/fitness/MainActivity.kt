package com.yourapp.fitness

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourapp.fitness.data.db.AppDatabase
import com.yourapp.fitness.data.repository.WorkoutRepository
import com.yourapp.fitness.domain.model.TrainingLevel
import com.yourapp.fitness.navigation.Screen
import com.yourapp.fitness.ui.history.HistoryViewModel
import com.yourapp.fitness.ui.onboarding.OnboardingScreen
import com.yourapp.fitness.ui.running.RunningScreen
import com.yourapp.fitness.ui.running.RunningViewModel
import com.yourapp.fitness.ui.settings.SettingsScreen
import com.yourapp.fitness.ui.strength.StrengthScreen
import com.yourapp.fitness.ui.strength.StrengthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val database = AppDatabase.getDatabase(this)
        val repository = WorkoutRepository(database.workoutDao())
        val preferences = getSharedPreferences("fit_over_40_prefs", Context.MODE_PRIVATE)

        setContent {
            FitOver40Theme {
                MainScreen(
                    repository = repository,
                    hasOnboardedInitially = preferences.getBoolean("has_onboarded", false),
                    initialTrainingLevel = TrainingLevel.fromStorageKey(
                        preferences.getString("training_level", null)
                    ),
                    onOnboardingComplete = { level ->
                        preferences.edit()
                            .putBoolean("has_onboarded", true)
                            .putString("training_level", level.storageKey)
                            .apply()
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    repository: WorkoutRepository,
    hasOnboardedInitially: Boolean,
    initialTrainingLevel: TrainingLevel?,
    onOnboardingComplete: (TrainingLevel) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var hasOnboarded by rememberSaveable { mutableStateOf(hasOnboardedInitially) }
    var selectedTrainingLevel by rememberSaveable { mutableStateOf(initialTrainingLevel) }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    icon = { androidx.compose.material3.Icon(Icons.Default.History, contentDescription = "History") },
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
                    icon = { androidx.compose.material3.Icon(Icons.Default.DirectionsRun, contentDescription = "Running") },
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
                    icon = { androidx.compose.material3.Icon(Icons.Default.FitnessCenter, contentDescription = "Strength") },
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
                    icon = { androidx.compose.material3.Icon(Icons.Default.Settings, contentDescription = "Settings") },
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
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHostWithManualInjection(
                navController = navController,
                repository = repository,
                trainingLevel = selectedTrainingLevel ?: TrainingLevel.BeginnerFirstTimeEver,
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
    onTrainingLevelChanged: (TrainingLevel) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.History.route
    ) {
        composable(Screen.History.route) {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(repository) as T
                }
            }
            com.yourapp.fitness.ui.history.HistoryScreen(viewModel(factory = factory))
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
                onTrainingLevelSelected = onTrainingLevelChanged
            )
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
