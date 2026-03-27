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
import com.swily.gymtracker.ui.theme.DarkBg
import com.swily.gymtracker.ui.theme.DarkSurface
import com.swily.gymtracker.ui.theme.TextGray
import com.swily.gymtracker.viewmodel.CatalogViewModel

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
    val hideBottomBar = currentRoute in listOf("exercise_edit", "exercise_create", "program_edit", "program_create")

    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var selectedProgram by remember { mutableStateOf<Program?>(null) }

    // Запоминаем активную вкладку каталога (0 = Тренировки, 1 = Упражнения)
    var catalogTab by remember { mutableIntStateOf(0) }

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
                    onProgramEditClick = { program ->
                        selectedProgram = program
                        navController.navigate("program_edit")
                    },
                    onCreateProgram = {
                        selectedProgram = null
                        navController.navigate("program_create")
                    },
                    onExerciseDelete = { exercise ->
                        catalogViewModel.deleteExercise(exercise)
                    },
                    onProgramDelete = { program ->
                        catalogViewModel.deleteProgram(program)
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
                ProgramEditScreen(
                    program = null,
                    allExercises = exercises,
                    selectedExerciseIds = emptyList(),
                    onSave = { name, description, colorHex, exerciseIds ->
                        catalogViewModel.insertProgram(
                            Program(name = name, description = description, colorHex = colorHex, estimatedMinutes = 45),
                            exerciseIds
                        )
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("program_edit") {
                val exercises by catalogViewModel.allExercises.collectAsState(initial = emptyList())
                val programExercises by selectedProgram?.let {
                    catalogViewModel.getExercisesForProgram(it.id).collectAsState(initial = emptyList())
                } ?: remember { mutableStateOf(emptyList()) }

                ProgramEditScreen(
                    program = selectedProgram,
                    allExercises = exercises,
                    selectedExerciseIds = programExercises.map { it.exerciseId },
                    onSave = { name, description, colorHex, exerciseIds ->
                        selectedProgram?.let { program ->
                            catalogViewModel.updateProgram(
                                program.copy(name = name, description = description, colorHex = colorHex),
                                exerciseIds
                            )
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}