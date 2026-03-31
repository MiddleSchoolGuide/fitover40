package com.yourapp.fitness.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yourapp.fitness.ui.history.HistoryScreen
import com.yourapp.fitness.ui.running.RunningScreen
import com.yourapp.fitness.ui.strength.StrengthScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.History.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Running.route) {
            RunningScreen()
        }
        composable(Screen.Strength.route) {
            StrengthScreen()
        }
    }
}
