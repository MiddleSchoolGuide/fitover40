package com.tonytrim.fitover40.navigation

sealed class Screen(val route: String) {
    object History : Screen("history")
    object Running : Screen("running")
    object Strength : Screen("strength")
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}
