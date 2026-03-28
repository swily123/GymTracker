package com.swily.gymtracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.Program
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.ui.theme.ProgramColorPalette
import com.swily.gymtracker.data.model.Warmup
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import com.swily.gymtracker.ui.theme.ProgramColorPalette
import com.swily.gymtracker.ui.theme.toHex

fun randomProgramColor(): String {
    return ProgramColorPalette.random().toHex()
}

@Composable
fun ProgramEditScreen(
    program: Program? = null,
    allExercises: List<Exercise>,
    selectedExerciseIds: List<Long>,
    allWarmups: List<Warmup> = emptyList(),
    selectedWarmupId: Long? = null,
    onSave: (name: String, description: String, colorHex: String, exerciseIds: List<Long>, warmupId: Long?) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = program != null
    var name by remember { mutableStateOf(program?.name ?: "") }
    var description by remember { mutableStateOf(program?.description ?: "") }
    var selectedColor by remember { mutableStateOf(program?.colorHex ?: "") }
    var selected by remember(selectedExerciseIds) { mutableStateOf(selectedExerciseIds.toSet()) }
    var selectedWarmup by remember(selectedWarmupId) { mutableStateOf(selectedWarmupId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Шапка
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
            Text(
                text = if (isEditing) "Изменить тренировку" else "Новая тренировка",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Всё остальное в одном скроллируемом списке
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Название
            item {
                Text("Название тренировки", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Например: Push Day") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = DarkSurfaceLight,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = Orange,
                        focusedPlaceholderColor = TextDarkGray,
                        unfocusedPlaceholderColor = TextDarkGray
                    ),
                    singleLine = true
                )
            }

            // Описание
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Описание (необязательно)", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Например: Грудь, спина, плечи") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = DarkSurfaceLight,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = Orange,
                        focusedPlaceholderColor = TextDarkGray,
                        unfocusedPlaceholderColor = TextDarkGray
                    ),
                    singleLine = true
                )
            }

            // Цвет
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Цвет (необязательно)", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgramColorPalette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selectedColor == color.toHex()) {
                                        Modifier.border(2.dp, TextWhite, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColor = color.toHex() }
                        )
                    }
                }
            }

            // Разминка
            if (allWarmups.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Разминка (необязательно)", color = TextGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedWarmup == null) DarkSurface else DarkBg)
                            .border(
                                width = 1.dp,
                                color = if (selectedWarmup == null) Orange else DarkSurfaceLight,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedWarmup = null }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Без разминки",
                            color = if (selectedWarmup == null) TextWhite else TextGray,
                            fontSize = 14.sp
                        )
                    }
                }

                items(allWarmups) { warmup ->
                    val isSelected = selectedWarmup == warmup.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DarkSurface else DarkBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Orange else DarkSurfaceLight,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedWarmup = warmup.id }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = warmup.name,
                            color = if (isSelected) TextWhite else TextGray,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Упражнения
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Выбери упражнения (${selected.size} выбрано)",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            items(allExercises) { exercise ->
                val isSelected = selected.contains(exercise.id)
                ExerciseCheckItem(
                    exercise = exercise,
                    isSelected = isSelected,
                    onClick = {
                        selected = if (isSelected) {
                            selected - exercise.id
                        } else {
                            selected + exercise.id
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Кнопка сохранения
        val canSave = name.isNotBlank() && selected.isNotEmpty()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (canSave) Orange else DarkSurfaceLight)
                .clickable(enabled = canSave) {
                    val finalColor = selectedColor.ifBlank { randomProgramColor() }
                    onSave(name.trim(), description.trim(), finalColor, selected.toList(), selectedWarmup)
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEditing) "Сохранить" else "Создать тренировку",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ExerciseCheckItem(
    exercise: Exercise,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DarkSurface else DarkBg)
            .border(
                width = 1.dp,
                color = if (isSelected) Orange else DarkSurfaceLight,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                color = if (isSelected) TextWhite else TextGray,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Orange else DarkSurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text("✓", color = TextWhite, fontSize = 14.sp)
                }
            }
        }
    }
}