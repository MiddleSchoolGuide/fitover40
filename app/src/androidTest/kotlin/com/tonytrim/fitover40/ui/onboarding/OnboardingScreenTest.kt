package com.tonytrim.fitover40.ui.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import com.tonytrim.fitover40.FitOver40Theme
import com.tonytrim.fitover40.domain.model.TrainingLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onboardingFlow_advancesAndReturnsSelectedLevel() {
        val selectedLevelState = mutableStateOf<TrainingLevel?>(null)
        var completedLevel: TrainingLevel? = null

        composeRule.setContent {
            FitOver40Theme {
                OnboardingScreen(
                    selectedLevel = selectedLevelState.value,
                    onLevelSelected = { level -> selectedLevelState.value = level },
                    onGetStarted = { level -> completedLevel = level }
                )
            }
        }

        composeRule.onNodeWithText("Welcome to FitOver40").assertIsDisplayed()
        composeRule.onNodeWithText("Let's Go").performClick()

        composeRule.onNodeWithText("Health Disclaimer").assertIsDisplayed()
        composeRule.onNodeWithText("I Understand").performClick()

        composeRule.onNodeWithText("Training Levels").assertIsDisplayed()
        composeRule.onNodeWithText("Beginner (First Time Ever)").performClick()
        composeRule.waitForIdle()

        assertEquals(TrainingLevel.BeginnerFirstTimeEver, selectedLevelState.value)
        assertNull(completedLevel)

        composeRule.onNodeWithText("Start Beginner (First Time Ever)").performScrollTo()
        composeRule.onNodeWithText("Start Beginner (First Time Ever)").assertIsDisplayed()
        composeRule.onNodeWithText("Start Beginner (First Time Ever)").performClick()
        composeRule.waitForIdle()

        assertEquals(TrainingLevel.BeginnerFirstTimeEver, completedLevel)
    }
}
