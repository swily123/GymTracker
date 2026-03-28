package com.swily.gymtracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.swily.gymtracker.data.model.Warmup
import com.swily.gymtracker.data.model.WarmupExercise
import com.swily.gymtracker.ui.theme.*

@Composable
fun WarmupEditScreen(
    warmup: Warmup? = null,
    allWarmupExercises: List<WarmupExercise>,
    selectedExerciseIds: List<Long>,
    onSave: (name: String, exerciseIds: List<Long>) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = warmup != null
    var name by remember { mutableStateOf(warmup?.name ?: "") }
    var selected by remember(selectedExerciseIds) { mutableStateOf(selectedExerciseIds.toSet()) }
    var showErrors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
                text = if (isEditing) "Изменить разминку" else "Новая разминка",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Название разминки *", color = if (showErrors && name.isBlank()) Color(0xFFCF6679) else TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) showErrors = false
            },
            placeholder = { Text("Например: Верх тела") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (showErrors && name.isBlank()) Color(0xFFCF6679) else Orange,
                unfocusedBorderColor = if (showErrors && name.isBlank()) Color(0xFFCF6679) else DarkSurfaceLight,
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

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (showErrors && selected.isEmpty())
                "Выбери упражнения * (нужно выбрать хотя бы одно)"
            else
                "Выбери упражнения * (${selected.size} выбрано)",
            color = if (showErrors && selected.isEmpty()) Color(0xFFCF6679) else TextGray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allWarmupExercises) { exercise ->
                val isSelected = selected.contains(exercise.id)
                WarmupExerciseCheckItem(
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Orange)
                .clickable {
                    if (name.isBlank() || selected.isEmpty()) {
                        showErrors = true
                    } else {
                        onSave(name.trim(), selected.toList())
                    }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEditing) "Сохранить" else "Создать разминку",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun WarmupExerciseCheckItem(
    exercise: WarmupExercise,
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
            Column {
                Text(
                    text = exercise.name,
                    color = if (isSelected) TextWhite else TextGray,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (exercise.description.isNotBlank()) {
                    Text(
                        text = exercise.description,
                        color = TextDarkGray,
                        fontSize = 12.sp
                    )
                }
            }
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