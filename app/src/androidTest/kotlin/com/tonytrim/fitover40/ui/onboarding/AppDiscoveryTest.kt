package com.tonytrim.fitover40.ui.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.tonytrim.fitover40.FitOver40Theme
import com.tonytrim.fitover40.domain.model.TrainingLevel
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class AppDiscoveryTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun welcomeScreen_exposesExpectedDiscoveryElements() {
        setOnboardingContent()

        composeRule.onNodeWithText("40+").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome to FitOver40").assertIsDisplayed()
        composeRule
            .onNodeWithText(
                "Interval running and strength training designed specifically for the 40+ athlete. No-fluff tracking that stays on your device."
            )
            .assertIsDisplayed()
        composeRule.onNodeWithText("Let's Go").assertIsDisplayed()
    }

    @Test
    fun clickingLetsGo_showsHealthDisclaimerOnSameHostActivity() {
        setOnboardingContent()
        val activity = composeRule.activity

        composeRule.onNodeWithText("Let's Go").performClick()

        assertSame(activity, composeRule.activity)
        composeRule.onNodeWithText("Health Disclaimer").assertIsDisplayed()
        composeRule
            .onNodeWithText(
                "Before starting any new exercise program, consult your physician or healthcare provider, especially if you have any pre-existing medical conditions. This app does not provide medical advice."
            )
            .assertIsDisplayed()
        composeRule.onNodeWithText("I Understand").assertIsDisplayed()
    }

    @Test
    fun acceptingDisclaimer_exposesLevelSelectionDiscoveryElements() {
        setOnboardingContent()

        composeRule.onNodeWithText("Let's Go").performClick()
        composeRule.onNodeWithText("I Understand").performClick()

        composeRule.onNodeWithText("Training Levels").assertIsDisplayed()
        composeRule.onNodeWithText("Run").assertIsDisplayed()
        composeRule.onNodeWithText("Lift").assertIsDisplayed()
        composeRule.onNodeWithText("Track").assertIsDisplayed()
        composeRule.onNodeWithText("Beginner (First Time Ever)").assertIsDisplayed()

        TrainingLevel.entries.forEach { level ->
            composeRule.onNodeWithText(level.displayName).assertIsDisplayed()
        }
    }

    private fun setOnboardingContent() {
        composeRule.setContent {
            FitOver40Theme {
                OnboardingScreen(
                    selectedLevel = null,
                    onLevelSelected = {},
                    onGetStarted = {}
                )
            }
        }
    }
}
