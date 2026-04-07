package com.tonytrim.fitover40.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonytrim.fitover40.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    SignIn,
    SignUp
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, error = null) }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, error = null) }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                if (state.mode == AuthMode.SignIn) {
                    authRepository.signIn(state.email.trim(), state.password)
                } else {
                    authRepository.signUp(state.email.trim(), state.password, state.name.trim().ifBlank { null })
                }
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        password = "",
                        confirmPassword = "",
                        error = null
                    )
                }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Unable to reach the sign-in service."
                    )
                }
            }
        }
    }

    private fun validate(state: AuthUiState): String? {
        if (!state.email.contains("@") || !state.email.contains(".")) return "Enter a valid email address."
        if (state.password.length < 8) return "Password must be at least 8 characters."
        if (state.mode == AuthMode.SignUp) {
            if (state.confirmPassword != state.password) return "Passwords do not match."
        }
        return null
    }
}
