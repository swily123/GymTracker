package com.swily.gymtracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Наша единственная цветовая схема — тёмная
private val GymColorScheme = darkColorScheme(
    primary = Orange,              // Главный акцент (кнопки)
    onPrimary = TextWhite,         // Текст НА оранжевых кнопках
    secondary = Green,             // Вторичный акцент (зелёные баннеры)
    onSecondary = TextWhite,       // Текст НА зелёных элементах
    background = DarkBg,           // Фон экрана
    onBackground = TextWhite,      // Текст НА фоне
    surface = DarkSurface,         // Фон карточек
    onSurface = TextWhite,         // Текст НА карточках
    surfaceVariant = DarkSurfaceLight, // Поля ввода, варианты
    onSurfaceVariant = TextGray,   // Вторичный текст
    error = Color(0xFFCF6679),     // Ошибки
    onError = TextWhite
)

@Composable
fun GymTrackerTheme(
    content: @Composable () -> Unit
) {
    // Делаем статус-бар тёмным (сливается с фоном)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = GymColorScheme,
        typography = Typography,
        content = content
    )
}