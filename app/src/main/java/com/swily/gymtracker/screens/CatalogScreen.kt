package com.swily.gymtracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.Program
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.CatalogViewModel

@Composable
fun CatalogScreen(viewModel: CatalogViewModel = viewModel()) {
    // Подписываемся на данные из ViewModel
    val programs by viewModel.allPrograms.collectAsState(initial = emptyList())
    val exercises by viewModel.allExercises.collectAsState(initial = emptyList())

    // Какая вкладка активна: 0 = Тренировки, 1 = Упражнения
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок
        Text(
            text = "Каталог",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Вкладки: Тренировки | Упражнения
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Тренировки",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            TabButton(
                text = "Упражнения",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Контент в зависимости от вкладки
        if (selectedTab == 0) {
            ProgramsList(programs = programs)
        } else {
            ExercisesList(exercises = exercises)
        }
    }
}

// Кнопка вкладки
@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Orange else DarkSurfaceLight)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = TextWhite,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Список программ тренировок
@Composable
fun ProgramsList(programs: List<Program>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(programs) { program ->
            ProgramCard(program = program)
        }

        // Кнопка "Создать свою программу"
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CreateButton(text = "Создать свою программу")
        }

        // Отступ внизу чтобы контент не прятался за навбар
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// Карточка программы
@Composable
fun ProgramCard(program: Program) {
    // Парсим цвет из hex-строки
    val cardColor = try {
        Color(android.graphics.Color.parseColor(program.colorHex))
    } catch (e: Exception) {
        Orange
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .clickable { /* TODO: запуск тренировки */ }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = program.name,
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = program.description,
                color = TextWhite.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "~${program.estimatedMinutes} мин",
                    color = TextWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Список упражнений
@Composable
fun ExercisesList(exercises: List<Exercise>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(exercises) { exercise ->
            ExerciseCard(exercise = exercise)
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            CreateButton(text = "Создать упражнение")
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// Карточка упражнения
@Composable
fun ExerciseCard(exercise: Exercise) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { /* TODO: редактирование упражнения */ }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = exercise.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${exercise.defaultReps} повторений · ${exercise.defaultWeightKg.toInt()} кг",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Кнопка "Создать..."
@Composable
fun CreateButton(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceLight)
            .clickable { /* TODO: создание */ }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ $text",
            color = TextGray,
            fontSize = 14.sp
        )
    }
}