package com.tonytrim.fitover40.ui.running

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonytrim.fitover40.data.db.RunWorkout
import com.tonytrim.fitover40.data.repository.WorkoutRepository
import com.tonytrim.fitover40.domain.model.BluetoothTreadmillDevice
import com.tonytrim.fitover40.domain.model.GeoPoint
import com.tonytrim.fitover40.domain.model.RunPlan
import com.tonytrim.fitover40.domain.model.RunningTrackingMode
import com.tonytrim.fitover40.domain.model.TrainingLevel
import com.tonytrim.fitover40.domain.model.TrainingPrograms
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class WorkoutPhase {
    WARM_UP, RUN, WALK, COOL_DOWN, FINISHED
}

data class RunningUiState(
    val trainingLevel: TrainingLevel = TrainingLevel.BeginnerFirstTimeEver,
    val planName: String = "",
    val planSummary: String = "",
    val effortCue: String = "",
    val trackingMode: RunningTrackingMode = RunningTrackingMode.Treadmill,
    val phase: WorkoutPhase = WorkoutPhase.WARM_UP,
    val secondsRemaining: Int = 0,
    val currentSet: Int = 1,
    val totalSets: Int = 1,
    val isPaused: Boolean = true,
    val workoutProgress: Float = 0f,
    val totalSecondsElapsed: Int = 0,
    val distanceMeters: Double = 0.0,
    val routePoints: List<GeoPoint> = emptyList(),
    val capturedSteps: Int = 0,
    val discoveredTreadmills: List<BluetoothTreadmillDevice> = emptyList(),
    val isBleScanning: Boolean = false,
    val connectedTreadmillName: String? = null,
    val ftmsSpeedKph: Double = 0.0,
    val ftmsDiagnostics: List<String> = emptyList(),
    val currentLocationLabel: String = "Waiting for movement data",
    val trackingStatus: String = "Treadmill mode uses your phone motion sensors.",
    val hasLocationFix: Boolean = false
)

class RunningViewModel(
    private val repository: WorkoutRepository,
    private val savedStateHandle: SavedStateHandle,
    private val trainingLevel: TrainingLevel
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentPlan: RunPlan? = null
    private var lastFtmsSampleTimestampMs: Long? = null

    init {
        currentPlan = TrainingPrograms.runningPlan(trainingLevel)
        if (savedStateHandle.get<Int>("seconds") == null) {
            resetWorkout()
        } else {
            restoreSavedState()
        }
    }

    fun setTrackingMode(mode: RunningTrackingMode) {
        if (_uiState.value.trackingMode == mode) return
        pauseTimer()
        _uiState.update {
            it.copy(
                trackingMode = mode,
                distanceMeters = 0.0,
                routePoints = emptyList(),
                capturedSteps = 0,
                ftmsSpeedKph = 0.0,
                currentLocationLabel = if (mode == RunningTrackingMode.Outdoor) {
                    "Waiting for GPS signal"
                } else {
                    it.connectedTreadmillName?.let { name -> "$name • 0.0 km/h" } ?: "Waiting for treadmill movement"
                },
                trackingStatus = if (mode == RunningTrackingMode.Outdoor) {
                    "Outside mode uses GPS to track route, distance, and current location."
                } else {
                    if (it.connectedTreadmillName != null) {
                        "Connected to ${it.connectedTreadmillName}. FTMS speed data will drive treadmill distance."
                    } else {
                        "Treadmill mode estimates distance from your phone motion sensors."
                    }
                },
                hasLocationFix = false
            )
        }
        resetWorkout()
    }

    fun updateTrackingStatus(status: String) {
        _uiState.update {
            it.copy(
                trackingStatus = status,
                ftmsDiagnostics = (listOf(status) + it.ftmsDiagnostics).distinct().take(6)
            )
        }
    }

    fun setBleScanning(isScanning: Boolean) {
        _uiState.update {
            it.copy(
                isBleScanning = isScanning,
                ftmsDiagnostics = (
                    listOf(if (isScanning) "BLE scan started" else "BLE scan stopped") + it.ftmsDiagnostics
                ).distinct().take(6)
            )
        }
    }

    fun setDiscoveredTreadmills(devices: List<BluetoothTreadmillDevice>) {
        _uiState.update { it.copy(discoveredTreadmills = devices) }
    }

    fun onTreadmillConnected(deviceName: String) {
        lastFtmsSampleTimestampMs = null
        _uiState.update {
            it.copy(
                connectedTreadmillName = deviceName,
                capturedSteps = 0,
                trackingStatus = "Connected to $deviceName. FTMS speed data will drive treadmill distance.",
                ftmsDiagnostics = (listOf("Connected to $deviceName") + it.ftmsDiagnostics).distinct().take(6),
                currentLocationLabel = "$deviceName • 0.0 km/h"
            )
        }
    }

    fun onTreadmillDisconnected() {
        lastFtmsSampleTimestampMs = null
        _uiState.update {
            it.copy(
                connectedTreadmillName = null,
                ftmsSpeedKph = 0.0,
                trackingStatus = "No FTMS treadmill connected. Falling back to phone motion sensors.",
                ftmsDiagnostics = (listOf("Treadmill disconnected") + it.ftmsDiagnostics).distinct().take(6),
                currentLocationLabel = "Waiting for treadmill movement"
            )
        }
    }

    fun onFtmsSpeedSample(speedKph: Double) {
        val now = System.currentTimeMillis()
        val state = _uiState.value
        val deltaMeters = if (
            state.trackingMode == RunningTrackingMode.Treadmill &&
            !state.isPaused &&
            state.phase != WorkoutPhase.FINISHED
        ) {
            val previous = lastFtmsSampleTimestampMs
            if (previous == null) 0.0 else ((now - previous).coerceAtMost(5_000).toDouble() / 3_600_000.0) * speedKph * 1000.0
        } else {
            0.0
        }
        lastFtmsSampleTimestampMs = now
        _uiState.update {
            it.copy(
                ftmsSpeedKph = speedKph,
                distanceMeters = it.distanceMeters + deltaMeters,
                ftmsDiagnostics = (
                    listOf("FTMS speed ${"%.1f".format(Locale.US, speedKph)} km/h") + it.ftmsDiagnostics
                ).distinct().take(6),
                currentLocationLabel = it.connectedTreadmillName?.let { name ->
                    "$name • ${"%.1f".format(Locale.US, speedKph)} km/h"
                } ?: it.currentLocationLabel
            )
        }
        persistUiState()
    }

    fun onStepCaptured() {
        val state = _uiState.value
        if (state.trackingMode == RunningTrackingMode.Treadmill && state.connectedTreadmillName != null) return
        if (state.isPaused || state.phase == WorkoutPhase.FINISHED) return

        val stride = trainingLevel.estimatedStrideMeters
        _uiState.update {
            val nextSteps = it.capturedSteps + 1
            val nextDistance = if (it.trackingMode == RunningTrackingMode.Treadmill) {
                nextSteps * stride
            } else {
                it.distanceMeters
            }
            it.copy(
                capturedSteps = nextSteps,
                distanceMeters = nextDistance,
                currentLocationLabel = if (it.trackingMode == RunningTrackingMode.Treadmill) {
                    "${nextSteps} steps captured"
                } else {
                    it.currentLocationLabel
                },
                trackingStatus = if (it.trackingMode == RunningTrackingMode.Treadmill) {
                    "Distance estimate uses ${"%.2f".format(Locale.US, stride)} m stride length for ${trainingLevel.displayName}."
                } else {
                    it.trackingStatus
                }
            )
        }
        persistUiState()
    }

    fun onOutdoorLocation(point: GeoPoint, accuracyMeters: Float) {
        val state = _uiState.value
        if (state.trackingMode != RunningTrackingMode.Outdoor || state.phase == WorkoutPhase.FINISHED) return

        // On Fire Tablets and devices without dedicated GPS, accuracy can often be > 35m initially
        // We'll relax this to 65m to show some progress on the map, but still update the status for all points
        val isAccuracyAcceptable = accuracyMeters <= 65f

        if (!isAccuracyAcceptable) {
            _uiState.update {
                it.copy(
                    trackingStatus = "Poor GPS accuracy: ${accuracyMeters.toInt()}m (need < 65m)",
                    hasLocationFix = true // We have a fix, it's just not good enough yet
                )
            }
            return
        }

        val lastPoint = state.routePoints.lastOrNull()
        val segmentDistance = if (lastPoint == null) {
            0f
        } else {
            FloatArray(1).also { result ->
                Location.distanceBetween(
                    lastPoint.latitude,
                    lastPoint.longitude,
                    point.latitude,
                    point.longitude,
                    result
                )
            }[0]
        }

        val isRecording = !state.isPaused
        val acceptedSegment = if (isRecording && (lastPoint == null || segmentDistance in 0.5f..150f)) segmentDistance.toDouble() else 0.0
        val shouldAppend = isRecording && (lastPoint == null || acceptedSegment > 0.0)

        _uiState.update {
            it.copy(
                distanceMeters = it.distanceMeters + acceptedSegment,
                routePoints = if (shouldAppend) it.routePoints + point else it.routePoints,
                currentLocationLabel = "${"%.5f".format(Locale.US, point.latitude)}, ${"%.5f".format(Locale.US, point.longitude)}",
                trackingStatus = "GPS accuracy: ${accuracyMeters.toInt()}m",
                hasLocationFix = true
            )
        }
        persistUiState()
    }

    fun startPauseWorkout() {
        if (_uiState.value.isPaused) {
            startTimer()
        } else {
            pauseTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isPaused = false) }
        persistUiState()
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                tick()
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isPaused = true) }
        persistUiState()
        timerJob?.cancel()
    }

    private fun tick() {
        val currentState = _uiState.value
        if (currentState.secondsRemaining > 0) {
            _uiState.update {
                it.copy(
                    secondsRemaining = it.secondsRemaining - 1,
                    totalSecondsElapsed = it.totalSecondsElapsed + 1,
                    workoutProgress = calculateProgress(
                        currentSet = it.currentSet,
                        totalSets = it.totalSets,
                        phase = it.phase
                    )
                )
            }
            persistUiState()
        } else {
            nextPhase()
        }
    }

    private fun nextPhase() {
        val currentState = _uiState.value
        val plan = currentPlan ?: return

        when (currentState.phase) {
            WorkoutPhase.WARM_UP -> _uiState.update { it.copy(phase = WorkoutPhase.RUN, secondsRemaining = plan.runSeconds) }
            WorkoutPhase.RUN -> _uiState.update { it.copy(phase = WorkoutPhase.WALK, secondsRemaining = plan.walkSeconds) }
            WorkoutPhase.WALK -> {
                if (currentState.currentSet < plan.sets) {
                    _uiState.update {
                        it.copy(
                            phase = WorkoutPhase.RUN,
                            secondsRemaining = plan.runSeconds,
                            currentSet = it.currentSet + 1
                        )
                    }
                } else {
                    _uiState.update { it.copy(phase = WorkoutPhase.COOL_DOWN, secondsRemaining = plan.coolDownMinutes * 60) }
                }
            }
            WorkoutPhase.COOL_DOWN -> finishWorkout()
            WorkoutPhase.FINISHED -> Unit
        }
        persistUiState()
    }

    private fun finishWorkout() {
        pauseTimer()
        _uiState.update { it.copy(phase = WorkoutPhase.FINISHED, workoutProgress = 1f) }
        persistUiState()
        viewModelScope.launch {
            val currentState = _uiState.value
            repository.saveRunWorkout(
                RunWorkout(
                    date = System.currentTimeMillis(),
                    durationSeconds = currentState.totalSecondsElapsed,
                    intervalsCompleted = currentState.currentSet,
                    estimatedCalories = (currentState.totalSecondsElapsed / 60) * 7,
                    planName = currentPlan?.name ?: "Custom Run",
                    trackingMode = currentState.connectedTreadmillName?.let { "Bluetooth FTMS" } ?: currentState.trackingMode.displayName,
                    distanceMeters = currentState.distanceMeters
                )
            )
        }
    }

    fun resetWorkout() {
        val plan = currentPlan ?: return
        pauseTimer()
        lastFtmsSampleTimestampMs = null
        val previousState = _uiState.value
        val currentMode = previousState.trackingMode
        _uiState.value = RunningUiState(
            trainingLevel = trainingLevel,
            planName = plan.name,
            planSummary = plan.summary,
            effortCue = plan.effortCue,
            trackingMode = currentMode,
            phase = WorkoutPhase.WARM_UP,
            secondsRemaining = plan.warmUpMinutes * 60,
            totalSets = plan.sets,
            discoveredTreadmills = previousState.discoveredTreadmills,
            isBleScanning = previousState.isBleScanning,
            connectedTreadmillName = previousState.connectedTreadmillName,
            ftmsDiagnostics = previousState.ftmsDiagnostics,
            currentLocationLabel = if (currentMode == RunningTrackingMode.Outdoor) {
                "Waiting for GPS signal"
            } else {
                previousState.connectedTreadmillName?.let { name -> "$name • 0.0 km/h" } ?: "Waiting for treadmill movement"
            },
            trackingStatus = if (currentMode == RunningTrackingMode.Outdoor) {
                "Outside mode uses GPS to track route, distance, and current location."
            } else {
                if (previousState.connectedTreadmillName != null) {
                    "Connected to ${previousState.connectedTreadmillName}. FTMS speed data will drive treadmill distance."
                } else {
                    "Treadmill mode estimates distance from your phone motion sensors."
                }
            }
        )
        persistUiState()
    }

    private fun restoreSavedState() {
        val plan = currentPlan ?: return
        val mode = savedStateHandle.get<RunningTrackingMode>("trackingMode") ?: RunningTrackingMode.Treadmill
        _uiState.value = RunningUiState(
            trainingLevel = trainingLevel,
            planName = plan.name,
            planSummary = plan.summary,
            effortCue = plan.effortCue,
            trackingMode = mode,
            phase = savedStateHandle.get<WorkoutPhase>("phase") ?: WorkoutPhase.WARM_UP,
            secondsRemaining = savedStateHandle.get<Int>("seconds") ?: plan.warmUpMinutes * 60,
            currentSet = savedStateHandle.get<Int>("currentSet") ?: 1,
            totalSets = savedStateHandle.get<Int>("totalSets") ?: plan.sets,
            isPaused = savedStateHandle.get<Boolean>("isPaused") ?: true,
            totalSecondsElapsed = savedStateHandle.get<Int>("totalSecondsElapsed") ?: 0,
            distanceMeters = savedStateHandle.get<Double>("distanceMeters") ?: 0.0,
            routePoints = savedStateHandle.get<List<GeoPoint>>("routePoints") ?: emptyList(),
            capturedSteps = savedStateHandle.get<Int>("capturedSteps") ?: 0,
            connectedTreadmillName = savedStateHandle.get<String>("connectedTreadmillName"),
            ftmsSpeedKph = savedStateHandle.get<Double>("ftmsSpeedKph") ?: 0.0,
            currentLocationLabel = savedStateHandle.get<String>("currentLocationLabel")
                ?: if (mode == RunningTrackingMode.Outdoor) "Waiting for GPS signal" else "Waiting for treadmill movement",
            trackingStatus = savedStateHandle.get<String>("trackingStatus")
                ?: if (mode == RunningTrackingMode.Outdoor) "Outside mode uses GPS to track route, distance, and current location."
                else "Treadmill mode estimates distance from your phone motion sensors.",
            hasLocationFix = savedStateHandle.get<Boolean>("hasLocationFix") ?: false
        )
    }

    private fun persistUiState() {
        val state = _uiState.value
        savedStateHandle["phase"] = state.phase
        savedStateHandle["seconds"] = state.secondsRemaining
        savedStateHandle["currentSet"] = state.currentSet
        savedStateHandle["totalSets"] = state.totalSets
        savedStateHandle["isPaused"] = state.isPaused
        savedStateHandle["totalSecondsElapsed"] = state.totalSecondsElapsed
        savedStateHandle["trackingMode"] = state.trackingMode
        savedStateHandle["distanceMeters"] = state.distanceMeters
        savedStateHandle["routePoints"] = ArrayList(state.routePoints)
        savedStateHandle["capturedSteps"] = state.capturedSteps
        savedStateHandle["connectedTreadmillName"] = state.connectedTreadmillName
        savedStateHandle["ftmsSpeedKph"] = state.ftmsSpeedKph
        savedStateHandle["currentLocationLabel"] = state.currentLocationLabel
        savedStateHandle["trackingStatus"] = state.trackingStatus
        savedStateHandle["hasLocationFix"] = state.hasLocationFix
    }

    private fun calculateProgress(currentSet: Int, totalSets: Int, phase: WorkoutPhase): Float {
        val baseProgress = currentSet.toFloat() / totalSets.coerceAtLeast(1)
        return when (phase) {
            WorkoutPhase.WARM_UP -> 0f
            WorkoutPhase.RUN, WorkoutPhase.WALK -> baseProgress.coerceIn(0f, 1f)
            WorkoutPhase.COOL_DOWN -> 0.95f
            WorkoutPhase.FINISHED -> 1f
        }
    }
}
