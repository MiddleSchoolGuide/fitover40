package com.tonytrim.fitover40.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonytrim.fitover40.data.sync.WorkoutSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutSyncUiState(
    val isSyncing: Boolean = false,
    val lastResultMessage: String? = null,
    val error: String? = null
)

class WorkoutSyncViewModel(
    private val repository: WorkoutSyncRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutSyncUiState())
    val uiState: StateFlow<WorkoutSyncUiState> = _uiState.asStateFlow()

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null, lastResultMessage = null) }
            runCatching { repository.syncAllWorkouts() }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            error = null,
                            lastResultMessage = "Workout sync completed."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            error = error.message ?: "Workout sync failed.",
                            lastResultMessage = null
                        )
                    }
                }
        }
    }
}
