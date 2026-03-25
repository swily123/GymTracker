package com.swily.gymtracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swily.gymtracker.screens.CatalogScreen
import com.swily.gymtracker.screens.HomeScreen
import com.swily.gymtracker.screens.ProfileScreen
import com.swily.gymtracker.screens.ProgressScreen
import com.swily.gymtracker.ui.theme.DarkBg
import com.swily.gymtracker.ui.theme.DarkSurface

@Composable
fun AppNavigation() {
    // NavController — "менеджер сцен", запоминает где мы и куда идём
    val navController = rememberNavController()

    // Список табов для нижней панели
    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Catalog,
        BottomNavItem.Progress,
        BottomNavItem.Profile
    )

    // Scaffold — каркас экрана (как Canvas в Unity, задаёт структуру)
    Scaffold(
        containerColor = DarkBg,
        // Нижняя панель навигации
        bottomBar = {
            // Получаем текущий route чтобы подсветить активный таб
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = DarkSurface
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                // Чтобы не плодить экраны в стеке
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true    // не создавать дубли
                                restoreState = true       // восстановить состояние
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = DarkSurface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost — контейнер для экранов (решает что показывать)
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Связываем route с экраном — как SceneManager.LoadScene("home")
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Catalog.route) { CatalogScreen() }
            composable(BottomNavItem.Progress.route) { ProgressScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}