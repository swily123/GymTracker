package com.swily.gymtracker.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.ProgramExercise
import com.swily.gymtracker.data.model.SetLog
import com.swily.gymtracker.data.model.WarmupExercise
import com.swily.gymtracker.data.model.WorkoutSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// TODO: Сохранять состояние тренировки в БД для восстановления после закрытия приложения

enum class WorkoutState {
    WARMUP,         // Разминка
    EXERCISE,       // Основное упражнение
    RESTING,        // Таймер отдыха
    REST_FINISHED,  // Таймер кончился, диалог
    COMPLETED       // Тренировка завершена
}

data class CurrentExerciseInfo(
    val exercise: Exercise,
    val programExercise: ProgramExercise,
    val exerciseIndex: Int,
    val totalExercises: Int
)

data class CurrentWarmupInfo(
    val warmupExercise: WarmupExercise,
    val exerciseIndex: Int,
    val totalExercises: Int
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val exerciseDao = database.exerciseDao()
    private val programDao = database.programDao()
    private val programExerciseDao = database.programExerciseDao()
    private val workoutSessionDao = database.workoutSessionDao()
    private val setLogDao = database.setLogDao()
    private val settingsDao = database.settingsDao()
    private val warmupContentDao = database.warmupContentDao()

    // --- Состояние ---
    private val _state = MutableStateFlow(WorkoutState.WARMUP)
    val state: StateFlow<WorkoutState> = _state

    private val _currentExercise = MutableStateFlow<CurrentExerciseInfo?>(null)
    val currentExercise: StateFlow<CurrentExerciseInfo?> = _currentExercise

    private val _currentWarmup = MutableStateFlow<CurrentWarmupInfo?>(null)
    val currentWarmup: StateFlow<CurrentWarmupInfo?> = _currentWarmup

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet

    private val _totalVolume = MutableStateFlow(0f)
    val totalVolume: StateFlow<Float> = _totalVolume

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val _restSecondsLeft = MutableStateFlow(0)
    val restSecondsLeft: StateFlow<Int> = _restSecondsLeft

    private val _restTotalSeconds = MutableStateFlow(0)
    val restTotalSeconds: StateFlow<Int> = _restTotalSeconds

    private val _completedExercises = MutableStateFlow(0)
    val completedExercises: StateFlow<Int> = _completedExercises

    // Внутренние данные
    private var programExercises: List<ProgramExercise> = emptyList()
    private var exerciseMap: Map<Long, Exercise> = emptyMap()
    private var warmupExercises: List<WarmupExercise> = emptyList()
    private var currentIndex = 0
    private var warmupIndex = 0
    private var sessionId: Long = 0
    private var timerJob: Job? = null
    private var elapsedJob: Job? = null
    private var restBetweenSets = 90
    private var restBetweenExercises = 300
    private var hasWarmup = false
    private var vibrationEnabled = true

    fun startWorkout(programId: Long) {
        viewModelScope.launch {
            // Загружаем настройки
            val settings = settingsDao.getSettings().first()
            if (settings != null) {
                restBetweenSets = settings.restBetweenSetsSec
                restBetweenExercises = settings.restBetweenExercisesSec
                vibrationEnabled = settings.vibrationEnabled
            }

            // Загружаем программу
            val program = programDao.getProgramById(programId) ?: return@launch

            // Загружаем упражнения программы
            val peList = programExerciseDao.getExercisesForProgram(programId).first()
            if (peList.isEmpty()) return@launch
            programExercises = peList

            // Загружаем данные упражнений
            val exercises = mutableMapOf<Long, Exercise>()
            for (pe in peList) {
                val exercise = exerciseDao.getExerciseById(pe.exerciseId)
                if (exercise != null) {
                    exercises[exercise.id] = exercise
                }
            }
            exerciseMap = exercises

            // Проверяем разминку
            if (program.warmupId != null) {
                val wExercises = warmupContentDao.getExercisesForWarmup(program.warmupId).first()
                if (wExercises.isNotEmpty()) {
                    warmupExercises = wExercises
                    hasWarmup = true
                }
            }

            // Создаём сессию
            sessionId = workoutSessionDao.insertSession(
                WorkoutSession(
                    programId = programId,
                    startTime = System.currentTimeMillis(),
                    durationSeconds = 0,
                    totalVolumeKg = 0f,
                    exerciseCount = peList.size
                )
            )

            // Запускаем общий таймер
            startElapsedTimer()

            // Начинаем с разминки или с упражнений
            if (hasWarmup) {
                showWarmupExercise(0)
            } else {
                showExercise(0)
            }
        }
    }

    // --- Разминка ---

    private fun showWarmupExercise(index: Int) {
        warmupIndex = index
        val exercise = warmupExercises[index]
        _currentWarmup.value = CurrentWarmupInfo(
            warmupExercise = exercise,
            exerciseIndex = index,
            totalExercises = warmupExercises.size
        )
        _state.value = WorkoutState.WARMUP
    }

    fun onWarmupExerciseDone() {
        val isLast = warmupIndex >= warmupExercises.size - 1
        if (isLast) {
            // Разминка закончена → отдых 5 мин → первое упражнение
            _currentWarmup.value = null
            startRest(restBetweenExercises, afterWarmup = true)
        } else {
            // Следующее упражнение разминки (без отдыха)
            showWarmupExercise(warmupIndex + 1)
        }
    }

    // --- Основные упражнения ---

    private fun showExercise(index: Int) {
        currentIndex = index
        val pe = programExercises[index]
        val exercise = exerciseMap[pe.exerciseId] ?: return

        _currentExercise.value = CurrentExerciseInfo(
            exercise = exercise,
            programExercise = pe,
            exerciseIndex = index,
            totalExercises = programExercises.size
        )
        _currentSet.value = 1
        _state.value = WorkoutState.EXERCISE
    }

    fun onSetCompleted() {
        val info = _currentExercise.value ?: return
        val pe = info.programExercise

        viewModelScope.launch {
            setLogDao.insertSetLog(
                SetLog(
                    sessionId = sessionId,
                    exerciseId = pe.exerciseId,
                    exerciseIndex = currentIndex,
                    setNumber = _currentSet.value,
                    weightKg = pe.weightKg,
                    reps = pe.reps,
                    completedAt = System.currentTimeMillis()
                )
            )
        }

        _totalVolume.value += pe.weightKg * pe.reps

        val isLastSet = _currentSet.value >= pe.sets
        val isLastExercise = currentIndex >= programExercises.size - 1

        when {
            isLastSet && isLastExercise -> {
                _completedExercises.value = programExercises.size
                finishWorkout()
            }
            isLastSet -> {
                _completedExercises.value = currentIndex + 1
                startRest(restBetweenExercises)
            }
            else -> {
                startRest(restBetweenSets)
            }
        }
    }

    // Изменение веса для текущего подхода
    fun adjustWeight(newWeight: Float) {
        val info = _currentExercise.value ?: return
        val updatedPe = info.programExercise.copy(weightKg = newWeight)
        _currentExercise.value = info.copy(programExercise = updatedPe)
    }

    // Изменение повторений для текущего подхода
    fun adjustReps(newReps: Int) {
        val info = _currentExercise.value ?: return
        val updatedPe = info.programExercise.copy(reps = newReps)
        _currentExercise.value = info.copy(programExercise = updatedPe)
    }

    // Добавить подход
    fun addSet() {
        val info = _currentExercise.value ?: return
        val updatedPe = info.programExercise.copy(sets = info.programExercise.sets + 1)
        _currentExercise.value = info.copy(programExercise = updatedPe)
    }

    // Пропустить подход (перейти к следующему или к следующему упражнению)
    fun skipSet() {
        val info = _currentExercise.value ?: return
        val pe = info.programExercise
        val isLastSet = _currentSet.value >= pe.sets
        val isLastExercise = currentIndex >= programExercises.size - 1

        when {
            isLastSet && isLastExercise -> {
                _completedExercises.value = programExercises.size
                finishWorkout()
            }
            isLastSet -> {
                _completedExercises.value = currentIndex + 1
                showExercise(currentIndex + 1)
            }
            else -> {
                _currentSet.value += 1
            }
        }
    }

    // --- Отдых ---

    private var isAfterWarmup = false

    private fun startRest(seconds: Int, afterWarmup: Boolean = false) {
        isAfterWarmup = afterWarmup
        _restTotalSeconds.value = seconds
        _restSecondsLeft.value = seconds
        _state.value = WorkoutState.RESTING
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_restSecondsLeft.value > 0) {
                delay(1000)
                _restSecondsLeft.value -= 1
            }
            vibrate()
            _state.value = WorkoutState.REST_FINISHED
        }
    }

    fun onContinue() {
        timerJob?.cancel()

        if (isAfterWarmup) {
            isAfterWarmup = false
            showExercise(0)
            return
        }

        val info = _currentExercise.value ?: return
        val isLastSet = _currentSet.value >= info.programExercise.sets

        if (isLastSet) {
            showExercise(currentIndex + 1)
        } else {
            _currentSet.value += 1
            _state.value = WorkoutState.EXERCISE
        }
    }

    fun onAddMinute() {
        _restSecondsLeft.value += 60
        _state.value = WorkoutState.RESTING
        if (timerJob?.isActive != true) {
            timerJob = viewModelScope.launch {
                while (_restSecondsLeft.value > 0) {
                    delay(1000)
                    _restSecondsLeft.value -= 1
                }
                vibrate()
                _state.value = WorkoutState.REST_FINISHED
            }
        }
    }

    // --- Таймеры ---

    private fun startElapsedTimer() {
        elapsedJob?.cancel()
        elapsedJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    // --- Завершение ---

    private fun finishWorkout() {
        timerJob?.cancel()
        elapsedJob?.cancel()
        _state.value = WorkoutState.COMPLETED

        viewModelScope.launch {
            workoutSessionDao.updateSession(
                WorkoutSession(
                    id = sessionId,
                    programId = _currentExercise.value?.programExercise?.programId ?: 0,
                    startTime = System.currentTimeMillis() - (_elapsedSeconds.value * 1000L),
                    endTime = System.currentTimeMillis(),
                    durationSeconds = _elapsedSeconds.value,
                    totalVolumeKg = _totalVolume.value,
                    exerciseCount = programExercises.size
                )
            )
        }
    }

    // --- Утилиты ---

    fun formatTime(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    private fun vibrate() {
        if (!vibrationEnabled) return

        val context = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 300, 200, 300, 200, 500),
                    -1
                )
            )
        }
    }
}