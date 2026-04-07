package com.tonytrim.fitover40.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonytrim.fitover40.data.account.AccountProfile
import com.tonytrim.fitover40.data.account.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = false,
    val profile: AccountProfile? = null,
    val error: String? = null
)

class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.fetchProfile() }
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, profile = profile, error = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unable to load account.",
                            profile = null
                        )
                    }
                }
        }
    }
}
