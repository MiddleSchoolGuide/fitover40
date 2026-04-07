package com.tonytrim.fitover40.data.auth

import com.tonytrim.fitover40.data.pref.AuthPrefs
import com.tonytrim.fitover40.data.pref.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val authApi: AuthApi,
    private val authPrefs: AuthPrefs
) {
    val session: Flow<AuthSession?> = authPrefs.session

    suspend fun signIn(email: String, password: String): AuthSession {
        val result = withContext(Dispatchers.IO) {
            authApi.signIn(AuthPayload(email = email, password = password))
        }
        authPrefs.saveSession(result.session)
        return result.session
    }

    suspend fun signUp(email: String, password: String, displayName: String?): AuthSession {
        val result = withContext(Dispatchers.IO) {
            authApi.signUp(
                AuthPayload(
                    email = email,
                    password = password,
                    displayName = displayName
                )
            )
        }
        authPrefs.saveSession(result.session)
        return result.session
    }

    suspend fun ensureValidSession(): AuthSession? {
        val session = authPrefs.session.first() ?: return null
        if (!session.isExpiredOrMissing()) return session

        val refreshToken = session.refreshToken
        if (refreshToken.isNullOrBlank()) {
            authPrefs.clearSession()
            return null
        }

        return runCatching {
            withContext(Dispatchers.IO) {
                authApi.refresh(refreshToken).session
            }.also { authPrefs.saveSession(it) }
        }.getOrElse {
            authPrefs.clearSession()
            null
        }
    }

    suspend fun getAuthorizationHeaderValue(): String? {
        val session = ensureValidSession() ?: return null
        return "${session.tokenType ?: "Bearer"} ${session.accessToken}"
    }

    suspend fun signOut() {
        val session = authPrefs.session.first()
        withContext(Dispatchers.IO) {
            authApi.logout(session?.refreshToken)
        }
        authPrefs.clearSession()
    }

    private fun AuthSession.isExpiredOrMissing(): Boolean {
        if (accessToken.isBlank()) return true
        val expiry = expiresAtEpochSeconds ?: return false
        val now = System.currentTimeMillis() / 1000L
        return now >= (expiry - 60L)
    }
}
