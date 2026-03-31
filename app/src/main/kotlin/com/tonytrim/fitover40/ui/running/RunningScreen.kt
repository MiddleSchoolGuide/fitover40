package com.tonytrim.fitover40.ui.running

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tonytrim.fitover40.domain.model.BluetoothTreadmillDevice
import com.tonytrim.fitover40.domain.model.GeoPoint
import com.tonytrim.fitover40.domain.model.RunningTrackingMode
import com.tonytrim.fitover40.ui.components.AccessibleButton
import com.tonytrim.fitover40.ui.components.BigTimer
import com.tonytrim.fitover40.ui.components.PhaseLabel

@Composable
fun RunningScreen(
    viewModel: RunningViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val hasLocationPermission = hasLocationPermission(context)
    val hasActivityPermission = hasActivityRecognitionPermission(context)
    val hasBlePermission = hasBluetoothPermission(context)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        viewModel.updateTrackingStatus(
            if (granted) "Permission granted. Start the workout or scan for a treadmill."
            else "Required permission was denied for the selected tracking mode."
        )
    }

    val ftmsController = remember(context) {
        FtmsBluetoothController(
            context = context.applicationContext,
            onDevicesUpdated = viewModel::setDiscoveredTreadmills,
            onScanningChanged = viewModel::setBleScanning,
            onConnected = viewModel::onTreadmillConnected,
            onDisconnected = viewModel::onTreadmillDisconnected,
            onSpeedSample = viewModel::onFtmsSpeedSample,
            onStatus = viewModel::updateTrackingStatus
        )
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase != WorkoutPhase.WARM_UP && uiState.phase != WorkoutPhase.FINISHED) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        }
    }

    DisposableEffect(uiState.trackingMode, uiState.isPaused, uiState.phase, hasLocationPermission, hasActivityPermission) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        var sensorListener: SensorEventListener? = null
        var locationListener: LocationListener? = null

        val trackingActive = !uiState.isPaused && uiState.phase != WorkoutPhase.FINISHED

        if (trackingActive && uiState.trackingMode == RunningTrackingMode.Treadmill && uiState.connectedTreadmillName == null) {
            if (!hasActivityPermission) {
                viewModel.updateTrackingStatus("Motion permission is required for treadmill fallback tracking.")
            } else {
                val stepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
                if (stepDetector != null) {
                    sensorListener = object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                                val steps = event.values.firstOrNull()?.toInt() ?: 1
                                repeat(steps.coerceAtLeast(1)) { viewModel.onTreadmillStep() }
                            }
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                    }
                    sensorManager.registerListener(sensorListener, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
                } else {
                    viewModel.updateTrackingStatus("This device does not expose a step detector sensor for treadmill fallback tracking.")
                }
            }
        }

        if (trackingActive && uiState.trackingMode == RunningTrackingMode.Outdoor) {
            if (!hasLocationPermission) {
                viewModel.updateTrackingStatus("Location permission is required for outside tracking.")
            } else {
                locationListener = LocationListener { location ->
                    viewModel.onOutdoorLocation(
                        point = GeoPoint(location.latitude, location.longitude),
                        accuracyMeters = location.accuracy
                    )
                }
                val listener = locationListener
                if (locationManager != null && listener != null) {
                    runCatching {
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 3f, listener, Looper.getMainLooper())
                        }
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 5f, listener, Looper.getMainLooper())
                        }
                    }.onFailure {
                        viewModel.updateTrackingStatus("Unable to start location updates on this device.")
                    }
                }
            }
        }

        onDispose {
            sensorListener?.let { sensorManager?.unregisterListener(it) }
            locationListener?.let { locationManager?.removeUpdates(it) }
        }
    }

    DisposableEffect(Unit) {
        onDispose { ftmsController.close() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Running", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(uiState.planName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AssistChip(onClick = {}, enabled = false, label = { Text(if (uiState.isPaused) "Paused" else "Active") })
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RunningTrackingMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = {
                                viewModel.setTrackingMode(mode)
                                when (mode) {
                                    RunningTrackingMode.Outdoor -> if (!hasLocationPermission) {
                                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    }
                                    RunningTrackingMode.Treadmill -> if (!hasActivityPermission) {
                                        permissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
                                    }
                                }
                            },
                            label = { Text(mode.displayName) },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.trackingMode != mode
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(uiState.trainingLevel.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(uiState.planSummary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PhaseLabel(
                            phase = uiState.phase.name.replace("_", " "),
                            color = when (uiState.phase) {
                                WorkoutPhase.RUN -> Color(0xFFC75B39)
                                WorkoutPhase.WALK -> Color(0xFF4D8C57)
                                WorkoutPhase.WARM_UP, WorkoutPhase.COOL_DOWN -> Color(0xFF4E6FAE)
                                WorkoutPhase.FINISHED -> MaterialTheme.colorScheme.secondary
                            }
                        )
                        BigTimer(secondsRemaining = uiState.secondsRemaining)
                        LinearProgressIndicator(progress = uiState.workoutProgress.coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth())
                        Text(
                            text = if (uiState.phase == WorkoutPhase.FINISHED) "Session complete" else "Set ${uiState.currentSet} of ${uiState.totalSets}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard("Elapsed", formatElapsed(uiState.totalSecondsElapsed), Modifier.weight(1f))
                    MetricCard("Distance", formatDistance(uiState.distanceMeters), Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard("Mode", uiState.connectedTreadmillName?.let { "Bluetooth FTMS" } ?: uiState.trackingMode.displayName, Modifier.weight(1f))
                    MetricCard(
                        if (uiState.trackingMode == RunningTrackingMode.Treadmill) "Speed" else "Location",
                        if (uiState.trackingMode == RunningTrackingMode.Treadmill) {
                            if (uiState.connectedTreadmillName != null) "${"%.1f".format(uiState.ftmsSpeedKph)} km/h" else "${uiState.treadmillSteps} steps"
                        } else {
                            if (uiState.hasLocationFix) "Locked" else "Searching"
                        },
                        Modifier.weight(1f)
                    )
                }
            }

            if (uiState.trackingMode == RunningTrackingMode.Outdoor) {
                item {
                    if (!hasLocationPermission) {
                        PermissionCard(
                            title = "Location permission needed",
                            body = "Outside mode needs location access to track route and distance.",
                            buttonText = "Allow Location",
                            onClick = {
                                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        )
                    } else {
                        RouteMapCard(routePoints = uiState.routePoints, currentLocationLabel = uiState.currentLocationLabel)
                    }
                }
            } else {
                item {
                    TreadmillTrackingCard(
                        uiState = uiState,
                        hasActivityPermission = hasActivityPermission,
                        hasBlePermission = hasBlePermission,
                        onRequestActivityPermission = { permissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)) },
                        onRequestBlePermission = { permissionLauncher.launch(bluetoothPermissions()) },
                        onStartScan = {
                            if (!hasBlePermission) permissionLauncher.launch(bluetoothPermissions()) else ftmsController.startScan()
                        },
                        onStopScan = { ftmsController.stopScan() },
                        onConnect = { address ->
                            if (!hasBlePermission) permissionLauncher.launch(bluetoothPermissions()) else ftmsController.connect(address)
                        },
                        onDisconnect = { ftmsController.disconnect() }
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Tracking status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(uiState.trackingStatus, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccessibleButton(
                        onClick = { viewModel.startPauseWorkout() },
                        text = if (uiState.isPaused) "Resume Workout" else "Pause Workout",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    )
                    OutlinedButton(
                        onClick = { viewModel.resetWorkout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Reset Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    if (uiState.phase == WorkoutPhase.FINISHED) {
                        Text(
                            text = "Your run has been saved to history with distance and tracking mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TreadmillTrackingCard(
    uiState: RunningUiState,
    hasActivityPermission: Boolean,
    hasBlePermission: Boolean,
    onRequestActivityPermission: () -> Unit,
    onRequestBlePermission: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Treadmill tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (uiState.connectedTreadmillName != null) {
                    "Connected FTMS treadmill speed is driving distance tracking."
                } else {
                    "Pair to an FTMS treadmill over Bluetooth or fall back to phone motion sensors."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            if (!hasActivityPermission) {
                AccessibleButton(onClick = onRequestActivityPermission, text = "Allow Motion Access")
            }
            if (!hasBlePermission) {
                AccessibleButton(onClick = onRequestBlePermission, text = "Allow Bluetooth Access")
            }

            if (uiState.connectedTreadmillName != null) {
                Text(uiState.currentLocationLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AccessibleButton(onClick = onDisconnect, text = "Disconnect Treadmill")
            } else {
                AccessibleButton(
                    onClick = if (uiState.isBleScanning) onStopScan else onStartScan,
                    text = if (uiState.isBleScanning) "Stop Scan" else "Scan for FTMS Treadmills"
                )
                if (uiState.discoveredTreadmills.isEmpty()) {
                    Text(
                        text = "No FTMS treadmills discovered yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.discoveredTreadmills.forEach { device ->
                            TreadmillDeviceRow(device = device, onConnect = { onConnect(device.address) })
                        }
                    }
                }
            }

            FtmsDiagnosticsCard(uiState = uiState)
        }
    }
}

@Composable
private fun TreadmillDeviceRow(
    device: BluetoothTreadmillDevice,
    onConnect: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConnect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(device.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                Text("Connect", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyLarge)
            AccessibleButton(onClick = onClick, text = buttonText)
        }
    }
}

@Composable
private fun FtmsDiagnosticsCard(uiState: RunningUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("FTMS Diagnostics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Scan: ${if (uiState.isBleScanning) "active" else "idle"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Connection: ${uiState.connectedTreadmillName ?: "not connected"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Live speed: ${"%.1f".format(uiState.ftmsSpeedKph)} km/h",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Discovered devices: ${uiState.discoveredTreadmills.size}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (uiState.ftmsDiagnostics.isEmpty()) {
                Text(
                    text = "No FTMS events yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.ftmsDiagnostics.forEach { line ->
                    Text(
                        text = "\u2022 $line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteMapCard(
    routePoints: List<GeoPoint>,
    currentLocationLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Outdoor route map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                drawRoundRect(color = Color(0xFFE2F1E5), size = size)
                if (routePoints.size > 1) {
                    val minLat = routePoints.minOf { it.latitude }
                    val maxLat = routePoints.maxOf { it.latitude }
                    val minLon = routePoints.minOf { it.longitude }
                    val maxLon = routePoints.maxOf { it.longitude }
                    val latRange = (maxLat - minLat).takeIf { it > 0.0 } ?: 0.001
                    val lonRange = (maxLon - minLon).takeIf { it > 0.0 } ?: 0.001

                    val path = Path()
                    routePoints.forEachIndexed { index, point ->
                        val x = (((point.longitude - minLon) / lonRange) * (size.width * 0.82f) + size.width * 0.09f).toFloat()
                        val y = (size.height - (((point.latitude - minLat) / latRange) * (size.height * 0.82f) + size.height * 0.09f)).toFloat()
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = Color(0xFF2D6A4F), style = Stroke(width = 10f, cap = StrokeCap.Round))

                    fun toOffset(point: GeoPoint): Offset {
                        val x = (((point.longitude - minLon) / lonRange) * (size.width * 0.82f) + size.width * 0.09f).toFloat()
                        val y = (size.height - (((point.latitude - minLat) / latRange) * (size.height * 0.82f) + size.height * 0.09f)).toFloat()
                        return Offset(x, y)
                    }
                    drawCircle(Color(0xFF465A91), radius = 12f, center = toOffset(routePoints.first()))
                    drawCircle(Color(0xFFC75B39), radius = 14f, center = toOffset(routePoints.last()))
                }
            }
            Text(currentLocationLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatElapsed(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatDistance(distanceMeters: Double): String =
    if (distanceMeters >= 1000) String.format("%.2f km", distanceMeters / 1000.0) else "${distanceMeters.toInt()} m"

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun hasActivityRecognitionPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

private fun hasBluetoothPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

private fun bluetoothPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        emptyArray()
    }
