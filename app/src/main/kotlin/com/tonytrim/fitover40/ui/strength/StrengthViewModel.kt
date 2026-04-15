package com.tonytrim.fitover40.ui.strength

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonytrim.fitover40.data.db.ExerciseSet
import com.tonytrim.fitover40.data.db.StrengthWorkout
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import com.tonytrim.fitover40.domain.model.ExercisePlan
import com.tonytrim.fitover40.domain.model.TrainingLevel
import com.tonytrim.fitover40.domain.model.TrainingPrograms
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StrengthUiState(
    val trainingLevel: TrainingLevel = TrainingLevel.BeginnerFirstTimeEver,
    val planName: String = "",
    val currentExerciseIndex: Int = 0,
    val currentSetNumber: Int = 1,
    val isResting: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val exercises: List<ExercisePlan> = emptyList(),
    val actualWeight: Double = 0.0,
    val isWorkoutFinished: Boolean = false,
    val totalSetsCompleted: Int = 0,
    val logs: List<ExerciseSet> = emptyList()
)

class StrengthViewModel(
    private val repository: WorkoutRepository,
    private val trainingLevel: TrainingLevel
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrengthUiState())
    val uiState: StateFlow<StrengthUiState> = _uiState.asStateFlow()

    private var restTimerJob: Job? = null

    init {
        _uiState.update {
            it.copy(
                trainingLevel = trainingLevel,
                planName = TrainingPrograms.strengthPlanName(trainingLevel),
                exercises = TrainingPrograms.strengthExercises(trainingLevel)
            )
        }
    }

    fun completeSet(actualReps: Int, weight: Double) {
        val currentState = _uiState.value
        val currentExercise = currentState.exercises[currentState.currentExerciseIndex]
        
        val log = ExerciseSet(
            workoutId = 0, // Will be updated on save
            exerciseName = currentExercise.name,
            setNumber = currentState.currentSetNumber,
            plannedReps = currentExercise.reps,
            actualReps = actualReps,
            weight = weight,
            date = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(
                logs = it.logs + log,
                totalSetsCompleted = it.totalSetsCompleted + 1
            )
        }

        if (currentState.currentSetNumber < currentExercise.sets) {
            startRest(currentExercise.restSeconds)
        } else {
            nextExercise()
        }
    }

    private fun startRest(seconds: Int) {
        _uiState.update { it.copy(isResting = true, restSecondsRemaining = seconds) }
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restSecondsRemaining > 0) {
                delay(1000)
                _uiState.update { it.copy(restSecondsRemaining = it.restSecondsRemaining - 1) }
            }
            _uiState.update { 
                it.copy(
                    isResting = false, 
                    currentSetNumber = it.currentSetNumber + 1 
                ) 
            }
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isResting = false,
                restSecondsRemaining = 0,
                currentSetNumber = it.currentSetNumber + 1
            )
        }
    }

    private fun nextExercise() {
        val currentState = _uiState.value
        if (currentState.currentExerciseIndex < currentState.exercises.size - 1) {
            _uiState.update { it.copy(
                currentExerciseIndex = it.currentExerciseIndex + 1,
                currentSetNumber = 1,
                isResting = false
            )}
        } else {
            finishWorkout()
        }
    }

    private fun finishWorkout() {
        _uiState.update { it.copy(isWorkoutFinished = true) }
        viewModelScope.launch {
            val currentState = _uiState.value
            repository.saveStrengthWorkout(
                StrengthWorkout(
                    date = System.currentTimeMillis(),
                    durationSeconds = 0, // Simplified for now
                    planName = _uiState.value.planName
                ),
                currentState.logs
            )
        }
    }
}
