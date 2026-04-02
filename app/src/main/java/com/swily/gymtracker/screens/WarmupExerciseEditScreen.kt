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
import com.swily.gymtracker.data.model.WarmupExercise
import com.swily.gymtracker.ui.theme.*
import androidx.compose.foundation.border
import com.swily.gymtracker.data.model.WarmupExerciseCollection

@Composable
fun WarmupExerciseEditScreen(
    warmupExercise: WarmupExercise? = null,
    collections: List<WarmupExerciseCollection> = emptyList(),
    onSave: (WarmupExercise) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = warmupExercise != null
    var name by remember { mutableStateOf(warmupExercise?.name ?: "") }
    var description by remember { mutableStateOf(warmupExercise?.description ?: "") }
    var reps by remember { mutableStateOf(warmupExercise?.reps?.toString() ?: "") }
    var showErrors by remember { mutableStateOf(false) }
    var selectedCollectionId by remember { mutableStateOf(warmupExercise?.collectionId) }

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

        Text("Название *", color = if (showErrors && name.isBlank()) Color(0xFFCF6679) else TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank() && reps.isNotBlank()) showErrors = false
            },
            placeholder = { Text("Например: Круговые махи руками") },
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

        Spacer(modifier = Modifier.height(16.dp))

        Text("Описание", color = TextGray, fontSize = 13.sp)
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

        Text("Кол-во повторений *", color = if (showErrors && reps.isBlank()) Color(0xFFCF6679) else TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it.filter { c -> c.isDigit() }
                if (name.isNotBlank() && reps.isNotBlank()) showErrors = false
            },
            modifier = Modifier.fillMaxWidth(0.4f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (showErrors && reps.isBlank()) Color(0xFFCF6679) else Orange,
                unfocusedBorderColor = if (showErrors && reps.isBlank()) Color(0xFFCF6679) else DarkSurfaceLight,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = Orange
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        if (collections.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Коллекция (необязательно)", color = TextGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedCollectionId == null) DarkSurface else DarkBg)
                    .border(
                        width = 1.dp,
                        color = if (selectedCollectionId == null) Orange else DarkSurfaceLight,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedCollectionId = null }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Без коллекции",
                    color = if (selectedCollectionId == null) TextWhite else TextGray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            collections.forEach { collection ->
                val isSelected = selectedCollectionId == collection.id
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
                        .clickable { selectedCollectionId = collection.id }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = collection.name,
                        color = if (isSelected) TextWhite else TextGray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (name.isNotBlank()) Orange else DarkSurfaceLight)
                .clickable {
                    if (name.isBlank() || reps.isBlank()) {
                        showErrors = true
                    } else {
                        onSave(
                            WarmupExercise(
                                id = warmupExercise?.id ?: 0,
                                name = name.trim(),
                                description = description.trim(),
                                reps = reps.toIntOrNull() ?: 10,
                                collectionId = selectedCollectionId
                            )
                        )
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