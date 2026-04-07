package com.tonytrim.fitover40.data.account

import com.tonytrim.fitover40.data.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountRepository(
    private val accountApi: AccountApi,
    private val authRepository: AuthRepository
) {
    suspend fun fetchProfile(): AccountProfile {
        val authorization = authRepository.getAuthorizationHeaderValue()
            ?: throw IllegalStateException("No valid session available.")
        return withContext(Dispatchers.IO) {
            accountApi.fetchProfile(authorization)
        }
    }
}
