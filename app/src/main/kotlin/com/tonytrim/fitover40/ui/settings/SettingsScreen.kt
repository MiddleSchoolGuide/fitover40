package com.tonytrim.fitover40.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tonytrim.fitover40.BuildConfig
import com.tonytrim.fitover40.domain.model.TrainingLevel

@Composable
fun SettingsScreen(
    selectedLevel: TrainingLevel,
    onTrainingLevelSelected: (TrainingLevel) -> Unit,
    useMetricUnits: Boolean,
    onUnitsToggled: (Boolean) -> Unit,
    defaultRestSeconds: Int,
    onRestSecondsChanged: (Int) -> Unit,
    onClearHistory: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All Workout History") },
            text = { Text("This will permanently delete all your workout history. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Preferences Section
            SettingsSection(title = "Preferences") {
                SettingsToggleRow(
                    title = "Use Metric Units (km)",
                    checked = useMetricUnits,
                    onCheckedChange = onUnitsToggled
                )
                
                SettingsRestTimeRow(
                    seconds = defaultRestSeconds,
                    onChanged = onRestSecondsChanged
                )

                SettingsButtonRow(
                    title = "Notification Permissions",
                    icon = Icons.Default.Notifications,
                    onClick = onRequestNotificationPermission
                )
            }

            // Account & Data Section
            SettingsSection(title = "Data & Privacy") {
                SettingsButtonRow(
                    title = "Clear All Workout History",
                    icon = Icons.Default.DeleteForever,
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { showClearHistoryDialog = true }
                )
                SettingsButtonRow(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = onPrivacyPolicyClick
                )
            }

            // Training Level Section
            SettingsSection(title = "Training Level") {
                TrainingLevel.entries.forEach { level ->
                    LevelSelectionCard(
                        level = level,
                        selected = selectedLevel == level,
                        onClick = { onTrainingLevelSelected(level) }
                    )
                }
            }

            // About Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FitOver40 Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun SettingsToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsRestTimeRow(seconds: Int, onChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Default Rest Time", style = MaterialTheme.typography.bodyLarge)
            Text("${seconds} seconds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (seconds > 0) onChanged(seconds - 5) }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            IconButton(onClick = { onChanged(seconds + 5) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

@Composable
private fun SettingsButtonRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = contentColor)
        Text(title, style = MaterialTheme.typography.bodyLarge, color = contentColor)
    }
}

@Composable
private fun LevelSelectionCard(
    level: TrainingLevel,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(12.dp)
            ) {}
            Column {
                Text(
                    text = level.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
