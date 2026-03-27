package com.swily.gymtracker.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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

// TODO: Запоминать позицию скролла при возврате из редактирования
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = viewModel(),
    initialTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {},
    onCreateExercise: () -> Unit = {},
    onProgramEditClick: (Program) -> Unit = {},
    onCreateProgram: () -> Unit = {},
    onProgramDelete: (Program) -> Unit = {},
    onExerciseDelete: (Exercise) -> Unit = {}
) {
    val programs by viewModel.allPrograms.collectAsState(initial = emptyList())
    val exercises by viewModel.allExercises.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Каталог",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Тренировки",
                isSelected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    onTabChanged(0)
                }
            )
            TabButton(
                text = "Упражнения",
                isSelected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    onTabChanged(1)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            ProgramsList(
                programs = programs,
                onProgramEditClick = onProgramEditClick,
                onProgramDelete = onProgramDelete,
                onCreateClick = onCreateProgram
            )
        } else {
            ExercisesList(
                exercises = exercises,
                onExerciseClick = onExerciseClick,
                onExerciseDelete = onExerciseDelete,
                onCreateClick = onCreateExercise
            )
        }
    }
}

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

@Composable
fun ProgramsList(
    programs: List<Program>,
    onProgramEditClick: (Program) -> Unit,
    onProgramDelete: (Program) -> Unit,
    onCreateClick: () -> Unit
) {
    // Диалог подтверждения удаления
    var programToDelete by remember { mutableStateOf<Program?>(null) }

    if (programToDelete != null) {
        AlertDialog(
            onDismissRequest = { programToDelete = null },
            title = { Text("Удалить программу?", color = TextWhite) },
            text = { Text("\"${programToDelete?.name}\" будет удалена навсегда", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    programToDelete?.let { onProgramDelete(it) }
                    programToDelete = null
                }) {
                    Text("Удалить", color = Orange)
                }
            },
            dismissButton = {
                TextButton(onClick = { programToDelete = null }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = DarkSurface
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(programs) { program ->
            ProgramCard(
                program = program,
                onEditClick = { onProgramEditClick(program) },
                onLongClick = { programToDelete = program }
            )
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CreateButton(text = "Создать свою программу", onClick = onCreateClick)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramCard(
    program: Program,
    onEditClick: () -> Unit,
    onLongClick: () -> Unit
) {
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
            .combinedClickable(
                onClick = { /* TODO: запуск тренировки */ },
                onLongClick = onLongClick
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = program.name,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✏", fontSize = 14.sp)
                }
            }
            if (program.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = program.description,
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                // TODO: Рассчитывать время тренировки на основе упражнений и отдыха
                text = "~${program.estimatedMinutes} мин",
                color = TextWhite.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ExercisesList(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseDelete: (Exercise) -> Unit,
    onCreateClick: () -> Unit
) {
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Удалить упражнение?", color = TextWhite) },
            text = { Text("\"${exerciseToDelete?.name}\" будет удалено навсегда", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    exerciseToDelete?.let { onExerciseDelete(it) }
                    exerciseToDelete = null
                }) {
                    Text("Удалить", color = Orange)
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = DarkSurface
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(exercises) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onClick = { onExerciseClick(exercise) },
                onLongClick = { exerciseToDelete = exercise }
            )
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CreateButton(text = "Создать упражнение", onClick = onCreateClick)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseCard(exercise: Exercise, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
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

@Composable
fun CreateButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceLight)
            .clickable { onClick() }
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