package com.tonytrim.fitover40.data.pref

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String?,
    val expiresAtEpochSeconds: Long?,
    val email: String,
    val userId: String?,
    val displayName: String?
)

class AuthPrefs(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        private val TOKEN_TYPE = stringPreferencesKey("auth_token_type")
        private val EXPIRES_AT_EPOCH_SECONDS = longPreferencesKey("auth_expires_at_epoch_seconds")
        private val EMAIL = stringPreferencesKey("auth_email")
        private val USER_ID = stringPreferencesKey("auth_user_id")
        private val DISPLAY_NAME = stringPreferencesKey("auth_display_name")
    }

    val session: Flow<AuthSession?> = context.dataStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN] ?: return@map null
        val email = preferences[EMAIL] ?: return@map null
        AuthSession(
            accessToken = accessToken,
            refreshToken = preferences[REFRESH_TOKEN],
            tokenType = preferences[TOKEN_TYPE],
            expiresAtEpochSeconds = preferences[EXPIRES_AT_EPOCH_SECONDS],
            email = email,
            userId = preferences[USER_ID],
            displayName = preferences[DISPLAY_NAME]
        )
    }

    suspend fun saveSession(session: AuthSession) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = session.accessToken
            session.refreshToken?.let { preferences[REFRESH_TOKEN] = it }
            session.tokenType?.let { preferences[TOKEN_TYPE] = it }
            session.expiresAtEpochSeconds?.let { preferences[EXPIRES_AT_EPOCH_SECONDS] = it }
            preferences[EMAIL] = session.email
            session.userId?.let { preferences[USER_ID] = it }
            session.displayName?.let { preferences[DISPLAY_NAME] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(TOKEN_TYPE)
            preferences.remove(EXPIRES_AT_EPOCH_SECONDS)
            preferences.remove(EMAIL)
            preferences.remove(USER_ID)
            preferences.remove(DISPLAY_NAME)
        }
    }
}
