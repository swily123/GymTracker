package com.swily.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.SetLog
import com.swily.gymtracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val exerciseDao = database.exerciseDao()
    private val setLogDao = database.setLogDao()
    private val bodyWeightLogDao = database.bodyWeightLogDao()

    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getLogsForExercise(exerciseId: Long): Flow<List<SetLog>> {
        return setLogDao.getLogsForExercise(exerciseId)
    }
}