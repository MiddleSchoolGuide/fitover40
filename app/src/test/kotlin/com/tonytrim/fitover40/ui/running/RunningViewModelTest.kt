package com.tonytrim.fitover40.ui.running

import androidx.lifecycle.SavedStateHandle
import com.tonytrim.fitover40.data.db.ExerciseSet
import com.tonytrim.fitover40.data.db.RunWorkout
import com.tonytrim.fitover40.data.db.StrengthWorkout
import com.tonytrim.fitover40.data.db.WorkoutDao
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import com.tonytrim.fitover40.domain.model.RunningTrackingMode
import com.tonytrim.fitover40.domain.model.TrainingLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class RunningViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RunningViewModel
    private lateinit var fakeRepository: WorkoutRepository

    @Before
    fun setup() {
        fakeRepository = WorkoutRepository(workoutDao = FakeWorkoutDao())
        viewModel = RunningViewModel(
            repository = fakeRepository,
            savedStateHandle = SavedStateHandle(),
            trainingLevel = TrainingLevel.BeginnerFirstTimeEver
        )
    }

    @Test
    fun `onStepCaptured in Treadmill mode increments steps and distance`() {
        viewModel.setTrackingMode(RunningTrackingMode.Treadmill)
        viewModel.startPauseWorkout() // Resume workout to allow tracking
        
        val initialSteps = viewModel.uiState.value.capturedSteps
        val initialDistance = viewModel.uiState.value.distanceMeters
        val stride = TrainingLevel.BeginnerFirstTimeEver.estimatedStrideMeters

        viewModel.onStepCaptured()

        assertEquals(initialSteps + 1, viewModel.uiState.value.capturedSteps)
        assertEquals(initialDistance + stride, viewModel.uiState.value.distanceMeters, 0.001)
    }

    @Test
    fun `onStepCaptured in Outdoor mode without GPS fix increments steps and estimated distance`() {
        viewModel.setTrackingMode(RunningTrackingMode.Outdoor)
        viewModel.startPauseWorkout() // Resume workout to allow tracking
        
        val initialSteps = viewModel.uiState.value.capturedSteps
        val initialDistance = viewModel.uiState.value.distanceMeters
        val stride = TrainingLevel.BeginnerFirstTimeEver.estimatedStrideMeters

        viewModel.onStepCaptured()

        assertEquals(initialSteps + 1, viewModel.uiState.value.capturedSteps)
        assertEquals(initialDistance + stride, viewModel.uiState.value.distanceMeters, 0.001)
    }

    @Test
    fun `onStepCaptured returns early if workout is paused`() {
        viewModel.setTrackingMode(RunningTrackingMode.Treadmill)
        // Initially paused
        
        val initialSteps = viewModel.uiState.value.capturedSteps
        val initialDistance = viewModel.uiState.value.distanceMeters

        viewModel.onStepCaptured()

        assertEquals(initialSteps, viewModel.uiState.value.capturedSteps)
        assertEquals(initialDistance, viewModel.uiState.value.distanceMeters, 0.001)
    }

    @Test
    fun `onStepCaptured returns early if treadmill is connected`() {
        viewModel.setTrackingMode(RunningTrackingMode.Treadmill)
        viewModel.startPauseWorkout()
        viewModel.onTreadmillConnected("Test Treadmill")
        
        val initialSteps = viewModel.uiState.value.capturedSteps
        val initialDistance = viewModel.uiState.value.distanceMeters

        viewModel.onStepCaptured()

        assertEquals(initialSteps, viewModel.uiState.value.capturedSteps)
        assertEquals(initialDistance, viewModel.uiState.value.distanceMeters, 0.001)
    }

    @Test
    fun `onStepCaptured in Outdoor mode should still count steps even if a treadmill is technically connected`() {
        viewModel.setTrackingMode(RunningTrackingMode.Outdoor)
        viewModel.startPauseWorkout()
        viewModel.onTreadmillConnected("Test Treadmill") // Suppose it was connected earlier
        
        val initialSteps = viewModel.uiState.value.capturedSteps
        val initialDistance = viewModel.uiState.value.distanceMeters
        val stride = TrainingLevel.BeginnerFirstTimeEver.estimatedStrideMeters

        viewModel.onStepCaptured()

        assertEquals(initialSteps + 1, viewModel.uiState.value.capturedSteps)
        assertEquals(initialDistance + stride, viewModel.uiState.value.distanceMeters, 0.001)
    }

    private class FakeWorkoutDao : WorkoutDao {
        override suspend fun insertRunWorkout(workout: RunWorkout): Long = 1L

        override suspend fun insertStrengthWorkout(workout: StrengthWorkout): Long = 1L

        override suspend fun insertExerciseSet(set: ExerciseSet) = Unit

        override fun getAllRunWorkouts(): Flow<List<RunWorkout>> = flowOf(emptyList())

        override fun getAllStrengthWorkouts(): Flow<List<StrengthWorkout>> = flowOf(emptyList())

        override fun getSetsForWorkout(workoutId: Long): Flow<List<ExerciseSet>> = flowOf(emptyList())

        override suspend fun getAllExerciseSets(): List<ExerciseSet> = emptyList()

        override suspend fun getPersonalRecord(name: String): Double? = null

        override fun getStreakCount(): Flow<Int> = flowOf(0)

        override suspend fun clearRunWorkouts() = Unit

        override suspend fun clearStrengthWorkouts() = Unit

        override suspend fun clearExerciseSets() = Unit
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    class MainDispatcherRule : TestWatcher() {
        private val dispatcher = StandardTestDispatcher()

        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
