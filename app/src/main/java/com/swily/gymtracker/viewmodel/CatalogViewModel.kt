package com.swily.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.Program
import com.swily.gymtracker.data.model.ProgramExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val exerciseDao = database.exerciseDao()
    private val programDao = database.programDao()
    private val programExerciseDao = database.programExerciseDao()

    val allPrograms: Flow<List<Program>> = programDao.getAllPrograms()
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    // --- Упражнения ---

    fun insertExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseDao.insertExercise(exercise)
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseDao.updateExercise(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseDao.deleteExercise(exercise)
        }
    }

    // --- Программы ---

    fun insertProgram(program: Program, exerciseIds: List<Long>) {
        viewModelScope.launch {
            val programId = programDao.insertProgram(program)
            val programExercises = exerciseIds.mapIndexed { index, exerciseId ->
                ProgramExercise(
                    programId = programId,
                    exerciseId = exerciseId,
                    orderIndex = index,
                    reps = 12,
                    weightKg = 0f
                )
            }
            programExerciseDao.insertAll(programExercises)
        }
    }

    fun updateProgram(program: Program, exerciseIds: List<Long>) {
        viewModelScope.launch {
            programDao.updateProgram(program)
            programExerciseDao.deleteByProgramId(program.id)
            val programExercises = exerciseIds.mapIndexed { index, exerciseId ->
                ProgramExercise(
                    programId = program.id,
                    exerciseId = exerciseId,
                    orderIndex = index,
                    reps = 12,
                    weightKg = 0f
                )
            }
            programExerciseDao.insertAll(programExercises)
        }
    }

    fun deleteProgram(program: Program) {
        viewModelScope.launch {
            programExerciseDao.deleteByProgramId(program.id)
            programDao.deleteProgram(program)
        }
    }

    fun getExercisesForProgram(programId: Long): Flow<List<ProgramExercise>> {
        return programExerciseDao.getExercisesForProgram(programId)
    }
}