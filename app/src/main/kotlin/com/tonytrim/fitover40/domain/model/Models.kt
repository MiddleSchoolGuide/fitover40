package com.tonytrim.fitover40.domain.model

import java.io.Serializable

data class RunPlan(
    val id: Long = 0,
    val name: String,
    val warmUpMinutes: Int,
    val runSeconds: Int,
    val walkSeconds: Int,
    val sets: Int,
    val coolDownMinutes: Int,
    val isPreset: Boolean = false,
    val summary: String = "",
    val effortCue: String = ""
) : Serializable

data class StrengthPlan(
    val id: Long = 0,
    val name: String,
    val exercises: List<ExercisePlan>
) : Serializable

data class ExercisePlan(
    val name: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val isJointFriendly: Boolean = true,
    val summary: String,
    val beginnerSetup: String,
    val formCues: List<String>,
    val easierOption: String,
    val mediaSourceName: String? = null,
    val videoUrl: String? = null,
    val illustration: ExerciseIllustration? = null
) : Serializable

enum class ExerciseIllustration {
    GluteBridge,
    BirdDog,
    WallPushUp,
    ChairSquat,
    BandRow
}

enum class TrainingLevel(
    val storageKey: String,
    val displayName: String,
    val description: String,
    val showsBeginnerMedia: Boolean,
    val estimatedStrideMeters: Double
) : Serializable {
    BeginnerFirstTimeEver(
        storageKey = "beginner_first_time_ever",
        displayName = "Beginner (First Time Ever)",
        description = "Brand new to training and building consistency from zero.",
        showsBeginnerMedia = true,
        estimatedStrideMeters = 0.62
    ),
    BeginnerBeenAWhile(
        storageKey = "beginner_been_a_while",
        displayName = "Beginner (Been A While)",
        description = "Returning after a long break and easing back into regular training.",
        showsBeginnerMedia = true,
        estimatedStrideMeters = 0.68
    ),
    IntermediateBeenAWhile(
        storageKey = "intermediate_been_a_while",
        displayName = "Intermediate (Been A While)",
        description = "Has prior experience but needs a controlled ramp back up.",
        showsBeginnerMedia = false,
        estimatedStrideMeters = 0.74
    ),
    Intermediate(
        storageKey = "intermediate",
        displayName = "Intermediate",
        description = "Comfortable with regular exercise and ready for structured progression.",
        showsBeginnerMedia = false,
        estimatedStrideMeters = 0.79
    ),
    Advanced(
        storageKey = "advanced",
        displayName = "Advanced",
        description = "Training consistently and able to handle longer work blocks and heavier strength work.",
        showsBeginnerMedia = false,
        estimatedStrideMeters = 0.84
    ),
    HighlyAdvanced(
        storageKey = "highly_advanced",
        displayName = "Highly Advanced",
        description = "Strong training background with capacity for demanding sessions.",
        showsBeginnerMedia = false,
        estimatedStrideMeters = 0.9
    );

    companion object {
        fun fromStorageKey(value: String?): TrainingLevel? =
            entries.firstOrNull { it.storageKey == value }
    }
}

enum class RunningTrackingMode(val displayName: String) {
    Treadmill("Treadmill"),
    Outdoor("Outside")
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) : Serializable

data class BluetoothTreadmillDevice(
    val name: String,
    val address: String
) : Serializable

data class WorkoutHistory(
    val id: Long,
    val date: Long,
    val type: String, // "RUN" or "STRENGTH"
    val durationMinutes: Int,
    val summary: String
) : Serializable
