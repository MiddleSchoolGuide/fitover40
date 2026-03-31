package com.tonytrim.fitover40.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonytrim.fitover40.data.db.RunWorkout
import com.tonytrim.fitover40.data.db.StrengthWorkout
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HistoryUiState(
    val runWorkouts: List<RunWorkout> = emptyList(),
    val strengthWorkouts: List<StrengthWorkout> = emptyList(),
    val streakCount: Int = 0,
    val weeklyCount: Int = 0,
    val weeklyMinutes: Int = 0
)

class HistoryViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllRunWorkouts(),
                repository.getAllStrengthWorkouts(),
                repository.getStreakCount()
            ) { runs, strengths, streak ->
                HistoryUiState(
                    runWorkouts = runs,
                    strengthWorkouts = strengths,
                    streakCount = streak,
                    weeklyCount = calculateWeeklyCount(runs, strengths),
                    weeklyMinutes = calculateWeeklyMinutes(runs, strengths)
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun calculateWeeklyCount(runs: List<RunWorkout>, strengths: List<StrengthWorkout>): Int {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        return runs.count { it.date > weekAgo } + strengths.count { it.date > weekAgo }
    }

    private fun calculateWeeklyMinutes(runs: List<RunWorkout>, strengths: List<StrengthWorkout>): Int {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        val runMins = runs.filter { it.date > weekAgo }.sumOf { it.durationSeconds / 60 }
        val strengthMins = strengths.filter { it.date > weekAgo }.sumOf { it.durationSeconds / 60 }
        return runMins + strengthMins
    }
}
