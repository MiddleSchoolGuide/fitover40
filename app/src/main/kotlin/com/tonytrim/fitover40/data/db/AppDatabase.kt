package com.tonytrim.fitover40.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertRunWorkout(workout: RunWorkout): Long

    @Insert
    suspend fun insertStrengthWorkout(workout: StrengthWorkout): Long

    @Insert
    suspend fun insertExerciseSet(set: ExerciseSet)

    @Query("SELECT * FROM run_workouts ORDER BY date DESC")
    fun getAllRunWorkouts(): Flow<List<RunWorkout>>

    @Query("SELECT * FROM strength_workouts ORDER BY date DESC")
    fun getAllStrengthWorkouts(): Flow<List<StrengthWorkout>>

    @Query("SELECT * FROM exercise_sets WHERE workoutId = :workoutId")
    fun getSetsForWorkout(workoutId: Long): Flow<List<ExerciseSet>>

    @Query("SELECT * FROM exercise_sets ORDER BY date DESC")
    suspend fun getAllExerciseSets(): List<ExerciseSet>

    @Query("SELECT MAX(weight) FROM exercise_sets WHERE exerciseName = :name")
    suspend fun getPersonalRecord(name: String): Double?

    @Query("SELECT COUNT(*) FROM (SELECT date FROM run_workouts UNION SELECT date FROM strength_workouts)")
    fun getStreakCount(): Flow<Int>

    @Query("DELETE FROM run_workouts")
    suspend fun clearRunWorkouts()

    @Query("DELETE FROM strength_workouts")
    suspend fun clearStrengthWorkouts()

    @Query("DELETE FROM exercise_sets")
    suspend fun clearExerciseSets()
}

@Database(
    entities = [RunWorkout::class, StrengthWorkout::class, ExerciseSet::class, WorkoutPlan::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
