package com.swily.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val workoutSessionDao = database.workoutSessionDao()

    val lastSession: Flow<WorkoutSession?> = workoutSessionDao.getLastSession()
}