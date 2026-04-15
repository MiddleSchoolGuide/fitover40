package com.tonytrim.fitover40.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

class OnboardingPrefs(private val context: Context) {

    companion object {
        val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
        val HEALTH_DISCLAIMER_ACCEPTED_AT = longPreferencesKey("health_disclaimer_accepted_at")
        val SELECTED_TRAINING_LEVEL = stringPreferencesKey("selected_training_level")
        // Stored in whole centimeters (e.g. 72 = 0.72 m). Null means use the training-level default.
        val CUSTOM_STRIDE_CM = intPreferencesKey("custom_stride_cm")
    }

    val hasOnboarded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_ONBOARDED] ?: false
    }

    val selectedTrainingLevel: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_TRAINING_LEVEL]
    }

    val customStrideCm: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_STRIDE_CM]
    }

    suspend fun saveOnboardingComplete(level: String) {
        context.dataStore.edit { preferences ->
            preferences[HAS_ONBOARDED] = true
            preferences[SELECTED_TRAINING_LEVEL] = level
            preferences[HEALTH_DISCLAIMER_ACCEPTED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun saveCustomStrideCm(cm: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_STRIDE_CM] = cm
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
