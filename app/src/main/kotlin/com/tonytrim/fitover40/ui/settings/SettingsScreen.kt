package com.tonytrim.fitover40.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tonytrim.fitover40.BuildConfig
import com.tonytrim.fitover40.domain.model.TrainingLevel

@Composable
fun SettingsScreen(
    accountUiState: AccountUiState,
    onRefreshAccount: () -> Unit,
    workoutSyncUiState: WorkoutSyncUiState,
    onSyncWorkouts: () -> Unit,
    signedInEmail: String?,
    signedInName: String?,
    selectedLevel: TrainingLevel,
    onTrainingLevelSelected: (TrainingLevel) -> Unit,
    useMetricUnits: Boolean,
    onUnitsToggled: (Boolean) -> Unit,
    defaultRestSeconds: Int,
    onRestSecondsChanged: (Int) -> Unit,
    onClearHistory: () -> Unit,
    onSignOut: () -> Unit,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsHeroCard(
                    selectedLevel = selectedLevel,
                    useMetricUnits = useMetricUnits,
                    defaultRestSeconds = defaultRestSeconds
                )
            }
            item {
                SettingsSection(title = "Account") {
                    SettingsActionRow(
                        title = accountUiState.profile?.displayName?.takeIf { it.isNotBlank() }
                            ?: signedInName?.takeIf { it.isNotBlank() }
                            ?: "Signed in",
                        subtitle = accountUiState.profile?.email
                            ?: signedInEmail
                            ?: "Connected to your backend account.",
                        icon = Icons.Default.Person,
                        onClick = {}
                    )

                    accountUiState.error?.let { error ->
                        Text(
                            text = error,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    SettingsDivider()

                    SettingsActionRow(
                        title = if (accountUiState.isLoading) "Refreshing account..." else "Refresh account status",
                        subtitle = "Calls /auth/me using your current bearer token.",
                        icon = Icons.Default.Sync,
                        onClick = onRefreshAccount
                    )

                    SettingsDivider()

                    SettingsActionRow(
                        title = "Sign out",
                        subtitle = "Remove the local session token from this device.",
                        icon = Icons.Default.Logout,
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = onSignOut
                    )
                }
            }
            item {
                SettingsSection(title = "Preferences") {
                    SettingsSwitchRow(
                        title = "Use metric units",
                        subtitle = "Show running distance in kilometers and meters.",
                        checked = useMetricUnits,
                        onCheckedChange = onUnitsToggled
                    )

                    SettingsDivider()

                    SettingsStepperRow(
                        title = "Default rest time",
                        subtitle = "$defaultRestSeconds seconds between strength sets.",
                        value = defaultRestSeconds,
                        onDecrement = { if (defaultRestSeconds > 15) onRestSecondsChanged(defaultRestSeconds - 5) },
                        onIncrement = { onRestSecondsChanged(defaultRestSeconds + 5) }
                    )

                    SettingsDivider()

                    SettingsActionRow(
                        title = "Notification permissions",
                        subtitle = "Enable workout reminders and timer alerts.",
                        icon = Icons.Default.Notifications,
                        onClick = onRequestNotificationPermission
                    )
                }
            }
            item {
                SettingsSection(title = "Data & Privacy") {
                    SettingsActionRow(
                        title = if (workoutSyncUiState.isSyncing) "Syncing workouts..." else "Sync workouts",
                        subtitle = "Push local runs and strength history to /workouts/sync.",
                        icon = Icons.Default.CloudUpload,
                        onClick = onSyncWorkouts
                    )

                    workoutSyncUiState.lastResultMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    workoutSyncUiState.error?.let { error ->
                        Text(
                            text = error,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    SettingsDivider()

                    SettingsActionRow(
                        title = "Privacy policy",
                        subtitle = "Review how workout and device data is handled.",
                        icon = Icons.Default.PrivacyTip,
                        onClick = onPrivacyPolicyClick
                    )

                    SettingsDivider()

                    SettingsActionRow(
                        title = "Clear all workout history",
                        subtitle = "Delete saved runs, lifts, and onboarding state.",
                        icon = Icons.Default.DeleteForever,
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearHistoryDialog = true }
                    )
                }
            }
            item {
                SettingsSection(title = "Training Level") {
                    TrainingLevel.entries.forEachIndexed { index, level ->
                        if (index > 0) {
                            SettingsDivider()
                        }
                        LevelSelectionRow(
                            level = level,
                            selected = selectedLevel == level,
                            onClick = { onTrainingLevelSelected(level) }
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
}

@Composable
private fun SettingsHeroCard(
    selectedLevel: TrainingLevel,
    useMetricUnits: Boolean,
    defaultRestSeconds: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tune training defaults and privacy controls without leaving the workout flow.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, enabled = false, label = { Text(selectedLevel.displayName) })
                AssistChip(onClick = {}, enabled = false, label = { Text(if (useMetricUnits) "Metric" else "Imperial") })
                AssistChip(onClick = {}, enabled = false, label = { Text("${defaultRestSeconds}s rest") })
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxSize()
        ) {}
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SettingsStepperRow(
    title: String,
    subtitle: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onDecrement) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease rest time")
                }
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilledTonalIconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Increase rest time")
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title, color = contentColor) },
        supportingContent = {
            Text(
                text = subtitle,
                color = if (contentColor == MaterialTheme.colorScheme.error) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        leadingContent = { Icon(icon, contentDescription = null, tint = contentColor) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun LevelSelectionRow(
    level: TrainingLevel,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = level.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (selected) {
            AssistChip(onClick = {}, enabled = false, label = { Text("Selected") })
        }
    }
}
