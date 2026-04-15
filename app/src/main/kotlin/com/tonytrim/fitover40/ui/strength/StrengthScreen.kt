package com.tonytrim.fitover40.ui.strength

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tonytrim.fitover40.domain.model.ExerciseIllustration
import com.tonytrim.fitover40.domain.model.ExercisePlan
import com.tonytrim.fitover40.ui.components.AccessibleButton
import com.tonytrim.fitover40.ui.components.RestTimer

@Composable
fun StrengthScreen(
    viewModel: StrengthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentExercise = uiState.exercises.getOrNull(uiState.currentExerciseIndex)

    if (uiState.isWorkoutFinished) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Workout finished",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You completed ${uiState.planName} with ${uiState.totalSetsCompleted} logged sets.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        return
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
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Strength",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.planName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = uiState.trainingLevel.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (currentExercise != null) {
                ExerciseDetailsCard(
                    exercise = currentExercise,
                    setNumber = uiState.currentSetNumber
                )

                if (uiState.isResting) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RestTimer(secondsRemaining = uiState.restSecondsRemaining)
                            Text(
                                text = "Keep your breathing steady and review the form cues before the next set.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            AccessibleButton(
                                onClick = { viewModel.skipRest() },
                                text = "Skip Rest"
                            )
                        }
                    }
                } else {
                    var weightInput by rememberSaveable(uiState.currentExerciseIndex, uiState.currentSetNumber) {
                        mutableStateOf("")
                    }
                    var repsInput by rememberSaveable(uiState.currentExerciseIndex, uiState.currentSetNumber) {
                        mutableStateOf(currentExercise.reps.toString())
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Log this set",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Weight") },
                                supportingText = { Text("Optional, enter kg or lb value") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )

                            OutlinedTextField(
                                value = repsInput,
                                onValueChange = { repsInput = it },
                                label = { Text("Actual reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }

                    AccessibleButton(
                        onClick = {
                            viewModel.completeSet(
                                repsInput.toIntOrNull() ?: currentExercise.reps,
                                weightInput.toDoubleOrNull() ?: 0.0
                            )
                        },
                        text = "Complete Set"
                    )

                    Text(
                        text = "${uiState.totalSetsCompleted} sets completed in this workout",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailsCard(
    exercise: ExercisePlan,
    setNumber: Int
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (exercise.illustration != null) {
                ExerciseIllustrationCard(exercise = exercise)
            }

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (exercise.isJointFriendly) {
                    AssistChip(onClick = {}, enabled = false, label = { Text("Joint-Friendly") })
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Set $setNumber of ${exercise.sets}") }
                )
            }

            Text(
                text = exercise.summary,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Target: ${exercise.reps} reps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            GuidanceBlock(
                title = "Get set",
                body = exercise.beginnerSetup
            )

            GuidanceBlock(
                title = "Form cues",
                items = exercise.formCues
            )

            GuidanceBlock(
                title = "If this feels hard",
                body = exercise.easierOption
            )

            if (exercise.videoUrl != null) {
                AccessibleButton(
                    onClick = { uriHandler.openUri(exercise.videoUrl) },
                    text = exercise.mediaSourceName?.let { "Watch Demo ($it)" } ?: "Watch Demo"
                )
            }
        }
    }
}

@Composable
private fun GuidanceBlock(
    title: String,
    body: String? = null,
    items: List<String> = emptyList()
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items.forEach { cue ->
                Text(
                    text = "\u2022 $cue",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ExerciseIllustrationCard(
    exercise: ExercisePlan
) {
    val illustration = exercise.illustration ?: return
    val colors = remember(exercise.illustration) {
        when (illustration) {
            ExerciseIllustration.GluteBridge -> listOf(Color(0xFFD8F3DC), Color(0xFFB7E4C7))
            ExerciseIllustration.BirdDog -> listOf(Color(0xFFFFE8D6), Color(0xFFFFD3B6))
            ExerciseIllustration.WallPushUp -> listOf(Color(0xFFDCEBFF), Color(0xFFBFD7FF))
            ExerciseIllustration.ChairSquat -> listOf(Color(0xFFFCE1E4), Color(0xFFF8C6CC))
            ExerciseIllustration.BandRow -> listOf(Color(0xFFE4F5E1), Color(0xFFCBEABD))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors),
                    shape = MaterialTheme.shapes.large
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Movement preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                ExerciseIllustrationCanvas(
                    illustration = illustration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun ExerciseIllustrationCanvas(
    illustration: ExerciseIllustration,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.04f
        val dark = Color(0xFF213547)
        val accent = Color(0xFF2D6A4F)
        val soft = Color(0xFF9C4F19)

        fun head(center: Offset, radius: Float) {
            drawCircle(color = dark, radius = radius, center = center, style = Stroke(width = stroke))
        }

        fun limb(start: Offset, end: Offset, color: Color = dark) {
            drawLine(color = color, start = start, end = end, strokeWidth = stroke, cap = StrokeCap.Round)
        }

        when (illustration) {
            ExerciseIllustration.GluteBridge -> {
                drawLine(
                    color = soft,
                    start = Offset(size.width * 0.12f, size.height * 0.76f),
                    end = Offset(size.width * 0.88f, size.height * 0.76f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                head(Offset(size.width * 0.26f, size.height * 0.48f), size.minDimension * 0.06f)
                limb(Offset(size.width * 0.31f, size.height * 0.5f), Offset(size.width * 0.42f, size.height * 0.58f))
                limb(Offset(size.width * 0.42f, size.height * 0.58f), Offset(size.width * 0.58f, size.height * 0.52f), accent)
                limb(Offset(size.width * 0.58f, size.height * 0.52f), Offset(size.width * 0.7f, size.height * 0.76f))
                limb(Offset(size.width * 0.44f, size.height * 0.58f), Offset(size.width * 0.34f, size.height * 0.72f))
                limb(Offset(size.width * 0.64f, size.height * 0.6f), Offset(size.width * 0.78f, size.height * 0.76f))
            }
            ExerciseIllustration.BirdDog -> {
                head(Offset(size.width * 0.28f, size.height * 0.28f), size.minDimension * 0.055f)
                limb(Offset(size.width * 0.32f, size.height * 0.32f), Offset(size.width * 0.45f, size.height * 0.42f))
                limb(Offset(size.width * 0.45f, size.height * 0.42f), Offset(size.width * 0.58f, size.height * 0.44f), accent)
                limb(Offset(size.width * 0.4f, size.height * 0.38f), Offset(size.width * 0.25f, size.height * 0.52f))
                limb(Offset(size.width * 0.53f, size.height * 0.43f), Offset(size.width * 0.67f, size.height * 0.3f), accent)
                limb(Offset(size.width * 0.46f, size.height * 0.45f), Offset(size.width * 0.36f, size.height * 0.74f))
                limb(Offset(size.width * 0.58f, size.height * 0.44f), Offset(size.width * 0.75f, size.height * 0.66f))
            }
            ExerciseIllustration.WallPushUp -> {
                drawRect(
                    color = soft.copy(alpha = 0.28f),
                    topLeft = Offset(size.width * 0.78f, size.height * 0.1f),
                    size = Size(size.width * 0.08f, size.height * 0.8f)
                )
                head(Offset(size.width * 0.34f, size.height * 0.34f), size.minDimension * 0.055f)
                limb(Offset(size.width * 0.37f, size.height * 0.38f), Offset(size.width * 0.52f, size.height * 0.48f))
                limb(Offset(size.width * 0.52f, size.height * 0.48f), Offset(size.width * 0.68f, size.height * 0.48f), accent)
                limb(Offset(size.width * 0.68f, size.height * 0.48f), Offset(size.width * 0.78f, size.height * 0.42f))
                limb(Offset(size.width * 0.51f, size.height * 0.49f), Offset(size.width * 0.44f, size.height * 0.72f))
                limb(Offset(size.width * 0.56f, size.height * 0.5f), Offset(size.width * 0.64f, size.height * 0.76f))
            }
            ExerciseIllustration.ChairSquat -> {
                drawRect(
                    color = soft.copy(alpha = 0.28f),
                    topLeft = Offset(size.width * 0.62f, size.height * 0.48f),
                    size = Size(size.width * 0.16f, size.height * 0.08f)
                )
                limb(Offset(size.width * 0.64f, size.height * 0.56f), Offset(size.width * 0.64f, size.height * 0.78f), soft)
                limb(Offset(size.width * 0.76f, size.height * 0.56f), Offset(size.width * 0.76f, size.height * 0.78f), soft)
                head(Offset(size.width * 0.42f, size.height * 0.26f), size.minDimension * 0.055f)
                limb(Offset(size.width * 0.44f, size.height * 0.3f), Offset(size.width * 0.5f, size.height * 0.47f))
                limb(Offset(size.width * 0.5f, size.height * 0.47f), Offset(size.width * 0.63f, size.height * 0.55f), accent)
                limb(Offset(size.width * 0.5f, size.height * 0.48f), Offset(size.width * 0.42f, size.height * 0.7f))
                limb(Offset(size.width * 0.63f, size.height * 0.55f), Offset(size.width * 0.55f, size.height * 0.76f))
            }
            ExerciseIllustration.BandRow -> {
                limb(Offset(size.width * 0.7f, size.height * 0.18f), Offset(size.width * 0.7f, size.height * 0.82f), soft)
                head(Offset(size.width * 0.36f, size.height * 0.28f), size.minDimension * 0.055f)
                limb(Offset(size.width * 0.38f, size.height * 0.33f), Offset(size.width * 0.42f, size.height * 0.52f))
                limb(Offset(size.width * 0.42f, size.height * 0.52f), Offset(size.width * 0.38f, size.height * 0.76f))
                limb(Offset(size.width * 0.42f, size.height * 0.52f), Offset(size.width * 0.5f, size.height * 0.76f))
                limb(Offset(size.width * 0.42f, size.height * 0.42f), Offset(size.width * 0.58f, size.height * 0.48f), accent)
                limb(Offset(size.width * 0.58f, size.height * 0.48f), Offset(size.width * 0.7f, size.height * 0.4f), accent)
            }
        }
    }
}
