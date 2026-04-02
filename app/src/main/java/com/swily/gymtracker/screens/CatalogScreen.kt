package com.swily.gymtracker.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.swily.gymtracker.data.model.Warmup
import com.swily.gymtracker.data.model.WarmupExercise
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.CatalogViewModel
import com.swily.gymtracker.WeightUtils
import com.swily.gymtracker.data.model.ExerciseCollection
import com.swily.gymtracker.data.model.WarmupExerciseCollection

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = viewModel(),
    useKg: Boolean = true,
    initialTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {},
    onCreateExercise: () -> Unit = {},
    onProgramEditClick: (Program) -> Unit = {},
    onCreateProgram: () -> Unit = {},
    onProgramDelete: (Program) -> Unit = {},
    onExerciseDelete: (Exercise) -> Unit = {},
    onProgramClick: (Program) -> Unit = {},
    onWarmupClick: (Warmup) -> Unit = {},
    onWarmupEditClick: (Warmup) -> Unit = {},
    onCreateWarmup: () -> Unit = {},
    onWarmupDelete: (Warmup) -> Unit = {},
    onWarmupExerciseClick: (WarmupExercise) -> Unit = {},
    onCreateWarmupExercise: () -> Unit = {},
    onWarmupExerciseDelete: (WarmupExercise) -> Unit = {},
    onCreateExerciseCollection: () -> Unit = {},
    onExerciseCollectionDelete: (ExerciseCollection) -> Unit = {},
    onCreateWarmupExerciseCollection: () -> Unit = {},
    onWarmupExerciseCollectionDelete: (WarmupExerciseCollection) -> Unit = {}
) {
    val programs by viewModel.allPrograms.collectAsState(initial = emptyList())
    val exercises by viewModel.allExercises.collectAsState(initial = emptyList())
    val warmups by viewModel.allWarmups.collectAsState(initial = emptyList())
    val warmupExercises by viewModel.allWarmupExercises.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val exerciseCollections by viewModel.allExerciseCollections.collectAsState(initial = emptyList())
    val warmupExerciseCollections by viewModel.allWarmupExerciseCollections.collectAsState(initial = emptyList())

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

        // Горизонтальный скролл для 4 вкладок
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Тренировки", "Упражнения", "Разминки", "Упражнения разминки")
            items(tabs.size) { index ->
                TabButton(
                    text = tabs[index],
                    isSelected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        onTabChanged(index)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ProgramsList(
                programs = programs,
                viewModel = viewModel,
                onProgramClick = onProgramClick,
                onProgramEditClick = onProgramEditClick,
                onProgramDelete = onProgramDelete,
                onCreateClick = onCreateProgram
            )
            1 -> ExercisesList(
                exercises = exercises,
                collections = exerciseCollections,
                useKg = useKg,
                onExerciseClick = onExerciseClick,
                onExerciseDelete = onExerciseDelete,
                onCreateClick = onCreateExercise,
                onCreateCollection = onCreateExerciseCollection,
                onCollectionDelete = onExerciseCollectionDelete
            )
            2 -> WarmupsList(
                warmups = warmups,
                onWarmupEditClick = onWarmupEditClick,
                onWarmupDelete = onWarmupDelete,
                onCreateClick = onCreateWarmup
            )
            3 -> WarmupExercisesList(
                exercises = warmupExercises,
                collections = warmupExerciseCollections,
                onExerciseClick = onWarmupExerciseClick,
                onExerciseDelete = onWarmupExerciseDelete,
                onCreateClick = onCreateWarmupExercise,
                onCreateCollection = onCreateWarmupExerciseCollection,
                onCollectionDelete = onWarmupExerciseCollectionDelete
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

// ==================== ПРОГРАММЫ ====================

@Composable
fun ProgramsList(
    programs: List<Program>,
    viewModel: CatalogViewModel,
    onProgramClick: (Program) -> Unit,
    onProgramEditClick: (Program) -> Unit,
    onProgramDelete: (Program) -> Unit,
    onCreateClick: () -> Unit
) {
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
                }) { Text("Удалить", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { programToDelete = null }) { Text("Отмена", color = TextGray) }
            },
            containerColor = DarkSurface
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(programs) { program ->
            ProgramCard(
                program = program,
                viewModel = viewModel,
                onClick = { onProgramClick(program) },
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
    viewModel: CatalogViewModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cardColor = try {
        Color(android.graphics.Color.parseColor(program.colorHex))
    } catch (e: Exception) { Orange }

    val estimatedMinutes by viewModel.calculateEstimatedMinutesFlow(program.id, program.warmupId)
        .collectAsState(initial = program.estimatedMinutes)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = program.name, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) { Text("✏", fontSize = 14.sp) }
            }
            if (program.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = program.description, color = TextWhite.copy(alpha = 0.8f), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "~${estimatedMinutes} мин",
                color = TextWhite.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

// ==================== УПРАЖНЕНИЯ ====================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseCard(exercise: Exercise, useKg: Boolean = true, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Column {
            Text(text = exercise.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${exercise.defaultReps} повторений · ${WeightUtils.format(exercise.defaultWeightKg, useKg)}",
                color = TextGray, fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ExercisesList(
    exercises: List<Exercise>,
    collections: List<ExerciseCollection> = emptyList(),
    useKg: Boolean = true,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseDelete: (Exercise) -> Unit,
    onCreateClick: () -> Unit,
    onCreateCollection: () -> Unit = {},
    onCollectionDelete: (ExerciseCollection) -> Unit = {}
) {
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var collectionToDelete by remember { mutableStateOf<ExerciseCollection?>(null) }
    var expandedCollections by remember { mutableStateOf(setOf<Long>()) }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Удалить упражнение?", color = TextWhite) },
            text = { Text("\"${exerciseToDelete?.name}\" будет удалено навсегда", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    exerciseToDelete?.let { onExerciseDelete(it) }
                    exerciseToDelete = null
                }) { Text("Удалить", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) { Text("Отмена", color = TextGray) }
            },
            containerColor = DarkSurface
        )
    }

    if (collectionToDelete != null) {
        AlertDialog(
            onDismissRequest = { collectionToDelete = null },
            title = { Text("Удалить коллекцию?", color = TextWhite) },
            text = { Text("\"${collectionToDelete?.name}\" будет удалена. Упражнения останутся без коллекции.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    collectionToDelete?.let { onCollectionDelete(it) }
                    collectionToDelete = null
                }) { Text("Удалить", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { collectionToDelete = null }) { Text("Отмена", color = TextGray) }
            },
            containerColor = DarkSurface
        )
    }

    val uncategorized = exercises.filter { it.collectionId == null }

    LazyColumn {
        // Коллекции
        items(collections) { collection ->
            val isExpanded = expandedCollections.contains(collection.id)
            val collectionExercises = exercises.filter { it.collectionId == collection.id }

            Spacer(modifier = Modifier.height(8.dp))
            CollectionHeader(
                name = collection.name,
                count = collectionExercises.size,
                isExpanded = isExpanded,
                onClick = {
                    expandedCollections = if (isExpanded) {
                        expandedCollections - collection.id
                    } else {
                        expandedCollections + collection.id
                    }
                },
                onLongClick = { collectionToDelete = collection }
            )

            if (isExpanded) {
                collectionExercises.forEach { exercise ->
                    Spacer(modifier = Modifier.height(4.dp))
                    ExerciseCard(
                        exercise = exercise,
                        useKg = useKg,
                        onClick = { onExerciseClick(exercise) },
                        onLongClick = { exerciseToDelete = exercise }
                    )
                }
            }
        }

        // Упражнения без коллекции
        if (uncategorized.isNotEmpty()) {
            if (collections.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Без коллекции", color = TextGray, fontSize = 13.sp)
                }
            }
            items(uncategorized) { exercise ->
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseCard(
                    exercise = exercise,
                    useKg = useKg,
                    onClick = { onExerciseClick(exercise) },
                    onLongClick = { exerciseToDelete = exercise }
                )
            }
        }

        // Кнопки создания
        item {
            Spacer(modifier = Modifier.height(12.dp))
            CreateButton(text = "Создать упражнение", onClick = onCreateClick)
            Spacer(modifier = Modifier.height(8.dp))
            CreateButton(text = "Создать коллекцию", onClick = onCreateCollection)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionHeader(
    name: String,
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpanded) "📂" else "📁",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$count",
                color = TextGray,
                fontSize = 14.sp
            )
        }
    }
}
// ==================== РАЗМИНКИ ====================

@Composable
fun WarmupsList(
    warmups: List<Warmup>,
    onWarmupEditClick: (Warmup) -> Unit,
    onWarmupDelete: (Warmup) -> Unit,
    onCreateClick: () -> Unit
) {
    var warmupToDelete by remember { mutableStateOf<Warmup?>(null) }

    if (warmupToDelete != null) {
        AlertDialog(
            onDismissRequest = { warmupToDelete = null },
            title = { Text("Удалить разминку?", color = TextWhite) },
            text = { Text("\"${warmupToDelete?.name}\" будет удалена навсегда", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    warmupToDelete?.let { onWarmupDelete(it) }
                    warmupToDelete = null
                }) { Text("Удалить", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { warmupToDelete = null }) { Text("Отмена", color = TextGray) }
            },
            containerColor = DarkSurface
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(warmups) { warmup ->
            WarmupCard(
                warmup = warmup,
                onClick = { onWarmupEditClick(warmup) },
                onLongClick = { warmupToDelete = warmup }
            )
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CreateButton(text = "Создать разминку", onClick = onCreateClick)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WarmupCard(warmup: Warmup, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Text(text = warmup.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

// ==================== УПРАЖНЕНИЯ РАЗМИНКИ ====================

@Composable
fun WarmupExercisesList(
    exercises: List<WarmupExercise>,
    collections: List<WarmupExerciseCollection> = emptyList(),
    onExerciseClick: (WarmupExercise) -> Unit,
    onExerciseDelete: (WarmupExercise) -> Unit,
    onCreateClick: () -> Unit,
    onCreateCollection: () -> Unit = {},
    onCollectionDelete: (WarmupExerciseCollection) -> Unit = {}
) {
    var exerciseToDelete by remember { mutableStateOf<WarmupExercise?>(null) }
    var collectionToDelete by remember { mutableStateOf<WarmupExerciseCollection?>(null) }
    var expandedCollections by remember { mutableStateOf(setOf<Long>()) }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Удалить упр. разминки?", color = TextWhite) },
            text = { Text("\"${exerciseToDelete?.name}\" будет удалено навсегда", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    exerciseToDelete?.let { onExerciseDelete(it) }
                    exerciseToDelete = null
                }) { Text("Удалить", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) { Text("Отмена", color = TextGray) }
            },
            containerColor = DarkSurface
        )
    }

    if (collectionToDelete != null) {
        AlertDialog(
            onDismissRequest = { collectionToDelete = null },
            title = { Text("Удалить коллекцию?", color = TextWhite) },
            text = { Text("\"${collectionToDelete?.name}\" будет удалена. Упражнения останутся без коллекции.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    collectionToDelete?.let { onCollectionDelete(it) }
                    collectionToDelete = null
                }) { Text("Удалить", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { collectionToDelete = null }) { Text("Отмена", color = TextGray) }
            },
            containerColor = DarkSurface
        )
    }

    val uncategorized = exercises.filter { it.collectionId == null }

    LazyColumn {
        items(collections) { collection ->
            val isExpanded = expandedCollections.contains(collection.id)
            val collectionExercises = exercises.filter { it.collectionId == collection.id }

            Spacer(modifier = Modifier.height(8.dp))
            CollectionHeader(
                name = collection.name,
                count = collectionExercises.size,
                isExpanded = isExpanded,
                onClick = {
                    expandedCollections = if (isExpanded) {
                        expandedCollections - collection.id
                    } else {
                        expandedCollections + collection.id
                    }
                },
                onLongClick = { collectionToDelete = collection }
            )

            if (isExpanded) {
                collectionExercises.forEach { exercise ->
                    Spacer(modifier = Modifier.height(4.dp))
                    WarmupExerciseCard(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise) },
                        onLongClick = { exerciseToDelete = exercise }
                    )
                }
            }
        }

        if (uncategorized.isNotEmpty()) {
            if (collections.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Без коллекции", color = TextGray, fontSize = 13.sp)
                }
            }
            items(uncategorized) { exercise ->
                Spacer(modifier = Modifier.height(8.dp))
                WarmupExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) },
                    onLongClick = { exerciseToDelete = exercise }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            CreateButton(text = "Создать упражнение разминки", onClick = onCreateClick)
            Spacer(modifier = Modifier.height(8.dp))
            CreateButton(text = "Создать коллекцию", onClick = onCreateCollection)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WarmupExerciseCard(
    exercise: WarmupExercise,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Column {
            Text(text = exercise.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${exercise.reps} повторений",
                color = TextGray, fontSize = 13.sp
            )
        }
    }
}

// ==================== ОБЩИЕ ====================

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
        Text(text = "+ $text", color = TextGray, fontSize = 14.sp)
    }
}