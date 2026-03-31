package com.swily.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.Settings
import com.swily.gymtracker.data.model.UserProfile
import com.swily.gymtracker.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val userProfileDao = database.userProfileDao()
    private val settingsDao = database.settingsDao()
    private val workoutSessionDao = database.workoutSessionDao()

    val profile: Flow<UserProfile?> = userProfileDao.getProfile()
    val settings: Flow<Settings?> = settingsDao.getSettings()
    val allSessions: Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    // Общее количество тренировок
    val totalWorkouts: Flow<Int> = allSessions.map { it.size }

    // Общий тоннаж
    val totalVolume: Flow<Float> = allSessions.map { sessions ->
        sessions.sumOf { it.totalVolumeKg.toDouble() }.toFloat()
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            userProfileDao.updateProfile(profile)
        }
    }

    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            settingsDao.updateSettings(settings)
        }
    }
}