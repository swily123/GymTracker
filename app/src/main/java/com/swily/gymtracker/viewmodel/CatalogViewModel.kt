package com.swily.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.Program
import kotlinx.coroutines.flow.Flow

// AndroidViewModel — как MonoBehaviour-синглтон менеджер
// Живёт пока жив экран, переживает повороты
class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val exerciseDao = database.exerciseDao()
    private val programDao = database.programDao()

    // Flow из DAO — автоматически обновит UI при изменении в БД
    val allPrograms: Flow<List<Program>> = programDao.getAllPrograms()
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
}