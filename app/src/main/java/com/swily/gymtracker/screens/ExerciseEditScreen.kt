package com.swily.gymtracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.ui.theme.*

@Composable
fun ExerciseEditScreen(
    exercise: Exercise? = null,  // null = создание, не null = редактирование
    onSave: (Exercise) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = exercise != null
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var reps by remember { mutableStateOf(exercise?.defaultReps?.toString() ?: "") }
    var weight by remember { mutableStateOf(exercise?.defaultWeightKg?.toInt()?.toString() ?: "") }
    var tip by remember { mutableStateOf(exercise?.tip ?: "") }
    var showErrors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Шапка с кнопкой назад
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                text = if (isEditing) "Изменить упражнение" else "Новое упражнение",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Поле: Название
        Text("Название упражнения *", color = if (showErrors && name.isBlank()) Color(0xFFCF6679) else TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) showErrors = false
            },
            placeholder = { Text("Например: Жим штанги лёжа") },
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

        Spacer(modifier = Modifier.height(24.dp))

        // Поля: Повторения и Вес
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Кол-во повторений", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it.filter { c -> c.isDigit() } },
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
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Рабочий вес (кг)", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() } },
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Совет по технике (необязательно)", color = TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = tip,
            onValueChange = { tip = it },
            placeholder = { Text("Например: Локти под 45°") },
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

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка сохранения
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (name.isNotBlank()) Orange else DarkSurfaceLight)
                .clickable {
                    if (name.isBlank()) {
                        showErrors = true
                    } else {
                        val newExercise = Exercise(
                            id = exercise?.id ?: 0,
                            name = name.trim(),
                            defaultReps = reps.toIntOrNull() ?: 12,
                            defaultWeightKg = (weight.toIntOrNull() ?: 0).toFloat(),
                            tip = tip.trim()
                        )
                        onSave(newExercise)
                    }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEditing) "Сохранить" else "Создать упражнение",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}