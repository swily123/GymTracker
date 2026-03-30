package com.swily.gymtracker.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.WorkoutState
import com.swily.gymtracker.viewmodel.WorkoutViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentExercise by viewModel.currentExercise.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val restSecondsLeft by viewModel.restSecondsLeft.collectAsState()
    val restTotalSeconds by viewModel.restTotalSeconds.collectAsState()
    val totalVolume by viewModel.totalVolume.collectAsState()
    val completedExercises by viewModel.completedExercises.collectAsState()
    val currentWarmup by viewModel.currentWarmup.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (state == WorkoutState.COMPLETED) {
            onFinished()
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Завершить тренировку?", color = TextWhite) },
            text = { Text("Прогресс текущей тренировки будет сохранён", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBack()
                }) {
                    Text("Завершить", color = Orange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Продолжить", color = Green)
                }
            },
            containerColor = DarkSurface
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        when (state) {
            WorkoutState.WARMUP -> {
                currentWarmup?.let { info ->
                    WarmupContent(
                        exerciseName = info.warmupExercise.name,
                        description = info.warmupExercise.description,
                        reps = info.warmupExercise.reps,
                        exerciseIndex = info.exerciseIndex + 1,
                        totalExercises = info.totalExercises,
                        elapsedTime = viewModel.formatTime(elapsedSeconds),
                        onDone = { viewModel.onWarmupExerciseDone() },
                        onBack = { showExitDialog = true }
                    )
                }
            }

            WorkoutState.EXERCISE -> {
                currentExercise?.let { info ->
                    ExerciseContent(
                        exerciseName = info.exercise.name,
                        currentSet = currentSet,
                        totalSets = info.programExercise.sets,
                        weightKg = info.programExercise.weightKg,
                        reps = info.programExercise.reps,
                        exerciseIndex = info.exerciseIndex + 1,
                        totalExercises = info.totalExercises,
                        elapsedTime = viewModel.formatTime(elapsedSeconds),
                        tip = info.exercise.tip,
                        onSetCompleted = { viewModel.onSetCompleted() },
                        onBack = { showExitDialog = true },
                        onWeightChange = { viewModel.adjustWeight(it) },
                        onRepsChange = { viewModel.adjustReps(it) },
                        onAddSet = { viewModel.addSet() },
                        onSkipSet = { viewModel.skipSet() }
                    )
                }
            }

            WorkoutState.RESTING -> {
                RestingContent(
                    secondsLeft = restSecondsLeft,
                    formattedTime = viewModel.formatTime(restSecondsLeft),
                    totalSeconds = restTotalSeconds,
                    onContinue = { viewModel.onContinue() },
                    onAddMinute = { viewModel.onAddMinute() }
                )
            }

            WorkoutState.REST_FINISHED -> {
                RestFinishedDialog(
                    onContinue = { viewModel.onContinue() },
                    onAddMinute = { viewModel.onAddMinute() }
                )
            }

            WorkoutState.COMPLETED -> {
                CompletedDialog(
                    elapsedTime = viewModel.formatTime(elapsedSeconds),
                    exerciseCount = currentExercise?.totalExercises ?: 0,
                    totalVolume = totalVolume,
                    onFinish = onFinished
                )
            }
        }
    }
}

@Composable
fun ExerciseContent(
    exerciseName: String,
    currentSet: Int,
    totalSets: Int,
    weightKg: Float,
    reps: Int,
    exerciseIndex: Int,
    totalExercises: Int,
    elapsedTime: String,
    tip: String,
    onSetCompleted: () -> Unit,
    onBack: () -> Unit,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onAddSet: () -> Unit,
    onSkipSet: () -> Unit
) {
    var showWeightDialog by remember { mutableStateOf(false) }
    var showRepsDialog by remember { mutableStateOf(false) }

    if (showWeightDialog) {
        NumberEditDialog(
            title = "Изменить вес (кг)",
            currentValue = "${weightKg.toInt()}",
            onConfirm = { newValue ->
                newValue.toFloatOrNull()?.let { onWeightChange(it) }
                showWeightDialog = false
            },
            onDismiss = { showWeightDialog = false }
        )
    }

    if (showRepsDialog) {
        NumberEditDialog(
            title = "Изменить повторения",
            currentValue = "$reps",
            onConfirm = { newValue ->
                newValue.toIntOrNull()?.let { onRepsChange(it) }
                showRepsDialog = false
            },
            onDismiss = { showRepsDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = TextWhite,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Упражнение", color = TextWhite, fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(elapsedTime, color = Green, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Прогресс", color = TextGray, fontSize = 12.sp)
            Text("$exerciseIndex/$totalExercises", color = TextGray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { exerciseIndex.toFloat() / totalExercises },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Orange,
            trackColor = DarkSurfaceLight,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exerciseName,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Подход $currentSet из $totalSets",
                color = TextGray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Редактируемые карточки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditableStatCard(
                value = "${weightKg.toInt()}",
                label = "кг",
                modifier = Modifier.weight(1f),
                onTap = { showWeightDialog = true }
            )
            EditableStatCard(
                value = "$reps",
                label = "повторений",
                modifier = Modifier.weight(1f),
                onTap = { showRepsDialog = true }
            )
            StatCard(
                value = "$currentSet/$totalSets",
                label = "подход",
                modifier = Modifier.weight(1f)
            )
        }

        // Кнопки управления подходами
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .clickable { onAddSet() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+1 подход", color = TextGray, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .clickable { onSkipSet() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Пропустить", color = TextGray, fontSize = 13.sp)
            }
        }

        if (tip.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Row {
                    Text("💡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = tip, color = TextGray, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Orange)
                .clickable { onSetCompleted() }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Подход выполнен ✓",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = Orange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun EditableStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onTap() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = Orange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "✎", color = TextGray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun NumberEditDialog(
    title: String,
    currentValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextWhite) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = DarkSurfaceLight,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = Orange
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text("OK", color = Orange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextGray)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun RestingContent(
    secondsLeft: Int,
    formattedTime: String,
    totalSeconds: Int,
    onContinue: () -> Unit,
    onAddMinute: () -> Unit
) {
    val targetProgress = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "rest_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(200.dp),
                strokeWidth = 8.dp,
                color = Orange,
                trackColor = DarkSurfaceLight,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    color = TextWhite,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Отдых", color = TextGray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Orange)
                .clickable { onContinue() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Пропустить →", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceLight)
                .clickable { onAddMinute() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+1 мин", color = TextGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun RestFinishedDialog(
    onContinue: () -> Unit,
    onAddMinute: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏰", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Время отдыха\nзакончилось!",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Готов к следующему подходу?", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Orange)
                        .clickable { onContinue() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Продолжить →", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceLight)
                        .clickable { onAddMinute() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+1 мин отдыха", color = TextGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun CompletedDialog(
    elapsedTime: String,
    exerciseCount: Int,
    totalVolume: Float,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Тренировка\nзавершена!",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Отличная работа!", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(value = elapsedTime, label = "время", modifier = Modifier.weight(1f))
                    StatCard(value = "$exerciseCount", label = "упражнений", modifier = Modifier.weight(1f))
                    StatCard(
                        value = String.format("%.1f т", totalVolume / 1000),
                        label = "объём",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green)
                        .clickable { onFinish() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Завершить ✓", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WarmupContent(
    exerciseName: String,
    description: String,
    reps: Int,
    exerciseIndex: Int,
    totalExercises: Int,
    elapsedTime: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = TextWhite,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Разминка", color = TextWhite, fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(elapsedTime, color = Green, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Разминка", color = TextGray, fontSize = 12.sp)
            Text("$exerciseIndex/$totalExercises", color = TextGray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { exerciseIndex.toFloat() / totalExercises },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Green,
            trackColor = DarkSurfaceLight,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🤸", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = exerciseName,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$reps повторений",
                color = Orange,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(16.dp)
                ) {
                    Text(text = description, color = TextGray, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Green)
                .clickable { onDone() }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Готово ✓",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}