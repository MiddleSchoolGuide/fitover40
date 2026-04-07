package com.tonytrim.fitover40.data.sync

import com.tonytrim.fitover40.data.auth.AuthRepository
import com.tonytrim.fitover40.data.repository.WorkoutRepository

class WorkoutSyncRepository(
    private val syncApi: WorkoutSyncApi,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) {
    suspend fun syncAllWorkouts() {
        val authorization = authRepository.getAuthorizationHeaderValue()
            ?: throw IllegalStateException("No valid session available.")
        val snapshot = workoutRepository.exportSyncSnapshot()
        syncApi.pushWorkouts(
            authorizationHeader = authorization,
            runWorkouts = snapshot.runWorkouts,
            strengthWorkouts = snapshot.strengthWorkouts,
            exerciseSets = snapshot.exerciseSets
        )
    }
}
