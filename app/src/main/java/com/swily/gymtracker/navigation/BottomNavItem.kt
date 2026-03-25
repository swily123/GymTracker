package com.swily.gymtracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Описываем каждый таб нижней навигации
// sealed class — как enum, но каждый вариант может иметь свои данные
sealed class BottomNavItem(
    val route: String,          // уникальный ID экрана (для NavController)
    val title: String,          // текст под иконкой
    val icon: ImageVector       // иконка
) {
    object Home : BottomNavItem("home", "Главная", Icons.Default.Home)
    object Catalog : BottomNavItem("catalog", "Каталог", Icons.AutoMirrored.Filled.List)
    object Progress : BottomNavItem("progress", "Прогресс", Icons.Default.DateRange)
    object Profile : BottomNavItem("profile", "Профиль", Icons.Default.Person)
}