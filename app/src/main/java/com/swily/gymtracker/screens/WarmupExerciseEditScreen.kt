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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swily.gymtracker.data.model.WarmupExercise
import com.swily.gymtracker.ui.theme.*

@Composable
fun WarmupExerciseEditScreen(
    warmupExercise: WarmupExercise? = null,
    onSave: (WarmupExercise) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = warmupExercise != null
    var name by remember { mutableStateOf(warmupExercise?.name ?: "") }
    var description by remember { mutableStateOf(warmupExercise?.description ?: "") }
    var reps by remember { mutableStateOf(warmupExercise?.reps?.toString() ?: "") }

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
                text = if (isEditing) "Изменить упр. разминки" else "Новое упр. разминки",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Название", color = TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Например: Круговые махи руками") },
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

        Spacer(modifier = Modifier.height(16.dp))

        Text("Описание (необязательно)", color = TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Как выполнять упражнение") },
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

        Spacer(modifier = Modifier.height(16.dp))

        Text("Кол-во повторений", color = TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it.filter { c -> c.isDigit() } },
            modifier = Modifier.fillMaxWidth(0.4f),
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

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (name.isNotBlank()) Orange else DarkSurfaceLight)
                .clickable(enabled = name.isNotBlank()) {
                    onSave(
                        WarmupExercise(
                            id = warmupExercise?.id ?: 0,
                            name = name.trim(),
                            description = description.trim(),
                            reps = reps.toIntOrNull() ?: 10
                        )
                    )
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