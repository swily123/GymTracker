package com.swily.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.navigation.AppNavigation
import com.swily.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Удаляем незавершённые тренировки при запуске
        lifecycleScope.launch {
            val database = GymDatabase.getDatabase(applicationContext)
            database.workoutSessionDao().deleteUnfinishedSessions()
        }

        setContent {
            GymTrackerTheme {
                AppNavigation()
            }
        }
    }
}