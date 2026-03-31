package com.yourapp.fitness.data.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "run_workouts")
data class RunWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val durationSeconds: Int,
    val intervalsCompleted: Int,
    val estimatedCalories: Int,
    val planName: String,
    val trackingMode: String,
    val distanceMeters: Double
)

@Keep
@Entity(tableName = "strength_workouts")
data class StrengthWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val durationSeconds: Int,
    val planName: String
)

@Keep
@Entity(tableName = "exercise_sets")
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val plannedReps: Int,
    val actualReps: Int,
    val weight: Double,
    val date: Long
)

@Keep
@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "RUN" or "STRENGTH"
    val configJson: String // Serialized plan details
)
