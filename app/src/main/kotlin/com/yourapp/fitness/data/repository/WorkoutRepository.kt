package com.yourapp.fitness.data.repository

import com.yourapp.fitness.data.db.*
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    fun getAllRunWorkouts(): Flow<List<RunWorkout>> = workoutDao.getAllRunWorkouts()
    
    fun getAllStrengthWorkouts(): Flow<List<StrengthWorkout>> = workoutDao.getAllStrengthWorkouts()

    suspend fun saveRunWorkout(workout: RunWorkout) {
        workoutDao.insertRunWorkout(workout)
    }

    suspend fun saveStrengthWorkout(workout: StrengthWorkout, sets: List<ExerciseSet>) {
        val workoutId = workoutDao.insertStrengthWorkout(workout)
        sets.forEach { set ->
            workoutDao.insertExerciseSet(set.copy(workoutId = workoutId))
        }
    }

    suspend fun getPersonalRecord(exerciseName: String): Double? {
        return workoutDao.getPersonalRecord(exerciseName)
    }

    fun getStreakCount(): Flow<Int> = workoutDao.getStreakCount()
}
