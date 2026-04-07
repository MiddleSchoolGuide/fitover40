package com.tonytrim.fitover40.data.sync

import com.tonytrim.fitover40.BuildConfig
import com.tonytrim.fitover40.data.db.ExerciseSet
import com.tonytrim.fitover40.data.db.RunWorkout
import com.tonytrim.fitover40.data.db.StrengthWorkout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WorkoutSyncApi(private val baseUrl: String) {

    fun pushWorkouts(
        authorizationHeader: String,
        runWorkouts: List<RunWorkout>,
        strengthWorkouts: List<StrengthWorkout>,
        exerciseSets: List<ExerciseSet>
    ) {
        check(baseUrl.isNotBlank()) {
            "Missing auth base URL. Set authBaseUrl in local.properties or FITOVER40_AUTH_BASE_URL."
        }

        val endpoint = baseUrl.trimEnd('/') + "/workouts/sync"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authorizationHeader)
        }

        val body = JSONObject().apply {
            put("deviceId", "android-local-device")
            put("appVersion", BuildConfig.VERSION_NAME)
            put("runWorkouts", JSONArray(runWorkouts.map { workout ->
                JSONObject().apply {
                    put("localId", workout.id)
                    put("date", workout.date)
                    put("durationSeconds", workout.durationSeconds)
                    put("intervalsCompleted", workout.intervalsCompleted)
                    put("estimatedCalories", workout.estimatedCalories)
                    put("planName", workout.planName)
                    put("trackingMode", workout.trackingMode)
                    put("distanceMeters", workout.distanceMeters)
                }
            }))
            put("strengthWorkouts", JSONArray(strengthWorkouts.map { workout ->
                JSONObject().apply {
                    put("localId", workout.id)
                    put("date", workout.date)
                    put("durationSeconds", workout.durationSeconds)
                    put("planName", workout.planName)
                }
            }))
            put("exerciseSets", JSONArray(exerciseSets.map { set ->
                JSONObject().apply {
                    put("localId", set.id)
                    put("workoutLocalId", set.workoutId)
                    put("exerciseName", set.exerciseName)
                    put("setNumber", set.setNumber)
                    put("plannedReps", set.plannedReps)
                    put("actualReps", set.actualReps)
                    put("weight", set.weight)
                    put("date", set.date)
                }
            }))
        }.toString()

        connection.outputStream.bufferedWriter().use { it.write(body) }

        val statusCode = connection.responseCode
        val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }.orEmpty()

        if (statusCode !in 200..299) {
            val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull()
            throw IllegalStateException(message?.takeIf { it.isNotBlank() }
                ?: "Workout sync failed with HTTP $statusCode.")
        }
    }
}
