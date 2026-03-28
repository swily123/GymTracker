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
import com.swily.gymtracker.data.model.Warmup
import com.swily.gymtracker.data.model.WarmupContent
import com.swily.gymtracker.data.model.WarmupExercise

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val exerciseDao = database.exerciseDao()
    private val programDao = database.programDao()
    private val programExerciseDao = database.programExerciseDao()
    private val warmupDao = database.warmupDao()
    private val warmupExerciseDao = database.warmupExerciseDao()
    private val warmupContentDao = database.warmupContentDao()

    val allWarmups: Flow<List<Warmup>> = warmupDao.getAllWarmups()
    val allWarmupExercises: Flow<List<WarmupExercise>> = warmupExerciseDao.getAllWarmupExercises()

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

    // --- Упражнения разминки ---

    fun insertWarmupExercise(warmupExercise: WarmupExercise) {
        viewModelScope.launch {
            warmupExerciseDao.insertWarmupExercise(warmupExercise)
        }
    }

    fun updateWarmupExercise(warmupExercise: WarmupExercise) {
        viewModelScope.launch {
            warmupExerciseDao.updateWarmupExercise(warmupExercise)
        }
    }

    fun deleteWarmupExercise(warmupExercise: WarmupExercise) {
        viewModelScope.launch {
            warmupExerciseDao.deleteWarmupExercise(warmupExercise)
        }
    }

    // --- Разминки ---

    fun insertWarmup(warmup: Warmup, warmupExerciseIds: List<Long>) {
        viewModelScope.launch {
            val warmupId = warmupDao.insertWarmup(warmup)
            val contents = warmupExerciseIds.mapIndexed { index, exerciseId ->
                WarmupContent(
                    warmupId = warmupId,
                    warmupExerciseId = exerciseId,
                    orderIndex = index
                )
            }
            warmupContentDao.insertAll(contents)
        }
    }

    fun updateWarmup(warmup: Warmup, warmupExerciseIds: List<Long>) {
        viewModelScope.launch {
            warmupDao.updateWarmup(warmup)
            warmupContentDao.deleteByWarmupId(warmup.id)
            val contents = warmupExerciseIds.mapIndexed { index, exerciseId ->
                WarmupContent(
                    warmupId = warmup.id,
                    warmupExerciseId = exerciseId,
                    orderIndex = index
                )
            }
            warmupContentDao.insertAll(contents)
        }
    }

    fun deleteWarmup(warmup: Warmup) {
        viewModelScope.launch {
            warmupContentDao.deleteByWarmupId(warmup.id)
            warmupDao.deleteWarmup(warmup)
        }
    }

    fun getExercisesForWarmup(warmupId: Long): Flow<List<WarmupExercise>> {
        return warmupContentDao.getExercisesForWarmup(warmupId)
    }
}