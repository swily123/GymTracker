package com.swily.gymtracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.Program
import com.swily.gymtracker.screens.*
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.CatalogViewModel
import com.swily.gymtracker.viewmodel.WorkoutViewModel
import com.swily.gymtracker.data.model.Warmup
import com.swily.gymtracker.data.model.WarmupExercise

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val catalogViewModel: CatalogViewModel = viewModel()

    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Catalog,
        BottomNavItem.Progress,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hideBottomBar = currentRoute in listOf(
        "exercise_edit", "exercise_create",
        "program_edit", "program_create",
        "workout", "timer",
        "warmup_edit", "warmup_create",
        "warmup_exercise_edit", "warmup_exercise_create"
    )

    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var selectedProgram by remember { mutableStateOf<Program?>(null) }
    var catalogTab by remember { mutableIntStateOf(0) }
    var workoutProgramId by remember { mutableStateOf<Long?>(null) }
    var selectedWarmup by remember { mutableStateOf<Warmup?>(null) }
    var selectedWarmupExercise by remember { mutableStateOf<WarmupExercise?>(null) }

    // Диалог подтверждения запуска тренировки
    var showStartWorkoutDialog by remember { mutableStateOf(false) }
    var pendingProgram by remember { mutableStateOf<Program?>(null) }

    if (showStartWorkoutDialog && pendingProgram != null) {
        AlertDialog(
            onDismissRequest = {
                showStartWorkoutDialog = false
                pendingProgram = null
            },
            title = { Text("Начать тренировку?", color = TextWhite) },
            text = { Text("\"${pendingProgram?.name}\"", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    showStartWorkoutDialog = false
                    workoutProgramId = pendingProgram?.id
                    pendingProgram = null
                    navController.navigate("workout")
                }) {
                    Text("Начать", color = Orange)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStartWorkoutDialog = false
                    pendingProgram = null
                }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = DarkSurface
        )
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(containerColor = DarkSurface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = DarkSurface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen() }

            composable(BottomNavItem.Catalog.route) {
                CatalogScreen(
                    viewModel = catalogViewModel,
                    initialTab = catalogTab,
                    onTabChanged = { catalogTab = it },
                    onExerciseClick = { exercise ->
                        selectedExercise = exercise
                        navController.navigate("exercise_edit")
                    },
                    onCreateExercise = {
                        selectedExercise = null
                        navController.navigate("exercise_create")
                    },
                    onProgramClick = { program ->
                        pendingProgram = program
                        showStartWorkoutDialog = true
                    },
                    onProgramEditClick = { program ->
                        selectedProgram = program
                        navController.navigate("program_edit")
                    },
                    onCreateProgram = {
                        selectedProgram = null
                        navController.navigate("program_create")
                    },
                    onProgramDelete = { program ->
                        catalogViewModel.deleteProgram(program)
                    },
                    onExerciseDelete = { exercise ->
                        catalogViewModel.deleteExercise(exercise)
                    },
                    onWarmupEditClick = { warmup ->
                        selectedWarmup = warmup
                        navController.navigate("warmup_edit")
                    },
                    onCreateWarmup = {
                        selectedWarmup = null
                        navController.navigate("warmup_create")
                    },
                    onWarmupDelete = { warmup ->
                        catalogViewModel.deleteWarmup(warmup)
                    },
                    onWarmupExerciseClick = { exercise ->
                        selectedWarmupExercise = exercise
                        navController.navigate("warmup_exercise_edit")
                    },
                    onCreateWarmupExercise = {
                        selectedWarmupExercise = null
                        navController.navigate("warmup_exercise_create")
                    },
                    onWarmupExerciseDelete = { exercise ->
                        catalogViewModel.deleteWarmupExercise(exercise)
                    }
                )
            }

            composable(BottomNavItem.Progress.route) { ProgressScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }

            composable("exercise_create") {
                ExerciseEditScreen(
                    exercise = null,
                    onSave = { exercise ->
                        catalogViewModel.insertExercise(exercise)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("exercise_edit") {
                ExerciseEditScreen(
                    exercise = selectedExercise,
                    onSave = { exercise ->
                        catalogViewModel.updateExercise(exercise)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("program_create") {
                val exercises by catalogViewModel.allExercises.collectAsState(initial = emptyList())
                val warmups by catalogViewModel.allWarmups.collectAsState(initial = emptyList())
                ProgramEditScreen(
                    program = null,
                    allExercises = exercises,
                    selectedExerciseIds = emptyList(),
                    allWarmups = warmups,
                    selectedWarmupId = null,
                    onSave = { name, description, colorHex, exerciseIds, warmupId ->
                        catalogViewModel.insertProgram(
                            Program(name = name, description = description, colorHex = colorHex, estimatedMinutes = 45),
                            exerciseIds,
                            warmupId
                        )
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("program_edit") {
                val exercises by catalogViewModel.allExercises.collectAsState(initial = emptyList())
                val warmups by catalogViewModel.allWarmups.collectAsState(initial = emptyList())
                val programExercises by selectedProgram?.let {
                    catalogViewModel.getExercisesForProgram(it.id).collectAsState(initial = emptyList())
                } ?: remember { mutableStateOf(emptyList()) }

                ProgramEditScreen(
                    program = selectedProgram,
                    allExercises = exercises,
                    selectedExerciseIds = programExercises.map { it.exerciseId },
                    allWarmups = warmups,
                    selectedWarmupId = selectedProgram?.warmupId,
                    onSave = { name, description, colorHex, exerciseIds, warmupId ->
                        selectedProgram?.let { program ->
                            catalogViewModel.updateProgram(
                                program.copy(name = name, description = description, colorHex = colorHex),
                                exerciseIds,
                                warmupId
                            )
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("workout") {
                val workoutViewModel: WorkoutViewModel = viewModel()

                LaunchedEffect(workoutProgramId) {
                    workoutProgramId?.let { programId ->
                        workoutViewModel.startWorkout(programId)
                    }
                }

                WorkoutScreen(
                    viewModel = workoutViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onFinished = {
                        navController.popBackStack()
                    }
                )
            }

            composable("warmup_exercise_create") {
                WarmupExerciseEditScreen(
                    warmupExercise = null,
                    onSave = { exercise ->
                        catalogViewModel.insertWarmupExercise(exercise)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("warmup_exercise_edit") {
                WarmupExerciseEditScreen(
                    warmupExercise = selectedWarmupExercise,
                    onSave = { exercise ->
                        catalogViewModel.updateWarmupExercise(exercise)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("warmup_create") {
                val warmupExercises by catalogViewModel.allWarmupExercises.collectAsState(initial = emptyList())
                WarmupEditScreen(
                    warmup = null,
                    allWarmupExercises = warmupExercises,
                    selectedExerciseIds = emptyList(),
                    onSave = { name, exerciseIds ->
                        catalogViewModel.insertWarmup(Warmup(name = name), exerciseIds)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("warmup_edit") {
                val warmupExercises by catalogViewModel.allWarmupExercises.collectAsState(initial = emptyList())
                val warmupContents by selectedWarmup?.let {
                    catalogViewModel.getExercisesForWarmup(it.id).collectAsState(initial = emptyList())
                } ?: remember { mutableStateOf(emptyList()) }

                WarmupEditScreen(
                    warmup = selectedWarmup,
                    allWarmupExercises = warmupExercises,
                    selectedExerciseIds = warmupContents.map { it.id },
                    onSave = { name, exerciseIds ->
                        selectedWarmup?.let { warmup ->
                            catalogViewModel.updateWarmup(warmup.copy(name = name), exerciseIds)
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onStartWorkout = {
                        navController.navigate(BottomNavItem.Catalog.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable("timer") {
                TimerScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onStartWorkout = {
                        navController.navigate(BottomNavItem.Catalog.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenTimer = {
                        navController.navigate("timer")
                    }
                )
            }
        }
    }
}