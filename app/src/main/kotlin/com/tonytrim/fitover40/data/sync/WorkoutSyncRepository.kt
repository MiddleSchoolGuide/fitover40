package com.tonytrim.fitover40.data.sync

import com.tonytrim.fitover40.data.auth.AuthRepository
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutSyncRepository(
    private val syncApi: WorkoutSyncApi,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) {
    suspend fun syncAllWorkouts() {
        val authorization = authRepository.getAuthorizationHeaderValue()
            ?: throw IllegalStateException("No valid session available.")
        val snapshot = workoutRepository.exportSyncSnapshot()
        withContext(Dispatchers.IO) {
            syncApi.pushWorkouts(
                authorizationHeader = authorization,
                runWorkouts = snapshot.runWorkouts,
                strengthWorkouts = snapshot.strengthWorkouts,
                exerciseSets = snapshot.exerciseSets
            )
        }
    }
}
