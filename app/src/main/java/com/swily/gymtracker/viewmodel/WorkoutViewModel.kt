package com.swily.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.data.model.ProgramExercise
import com.swily.gymtracker.data.model.SetLog
import com.swily.gymtracker.data.model.WorkoutSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// Состояния экрана тренировки
enum class WorkoutState {
    EXERCISE,       // Показываем упражнение
    RESTING,        // Таймер отдыха идёт
    REST_FINISHED,  // Таймер кончился, показываем диалог
    COMPLETED       // Тренировка завершена
}

// Вся информация о текущем упражнении в одном месте
data class CurrentExerciseInfo(
    val exercise: Exercise,
    val programExercise: ProgramExercise,
    val exerciseIndex: Int,    // какое по счёту (0, 1, 2...)
    val totalExercises: Int    // сколько всего
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GymDatabase.getDatabase(application)
    private val exerciseDao = database.exerciseDao()
    private val programExerciseDao = database.programExerciseDao()
    private val workoutSessionDao = database.workoutSessionDao()
    private val setLogDao = database.setLogDao()
    private val settingsDao = database.settingsDao()
    private val _restTotalSeconds = MutableStateFlow(0)
    val restTotalSeconds: StateFlow<Int> = _restTotalSeconds

    // --- Состояние тренировки ---
    private val _state = MutableStateFlow(WorkoutState.EXERCISE)
    val state: StateFlow<WorkoutState> = _state

    private val _currentExercise = MutableStateFlow<CurrentExerciseInfo?>(null)
    val currentExercise: StateFlow<CurrentExerciseInfo?> = _currentExercise

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet

    private val _totalVolume = MutableStateFlow(0f)
    val totalVolume: StateFlow<Float> = _totalVolume

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val _restSecondsLeft = MutableStateFlow(0)
    val restSecondsLeft: StateFlow<Int> = _restSecondsLeft

    private val _completedExercises = MutableStateFlow(0)
    val completedExercises: StateFlow<Int> = _completedExercises

    // Внутренние данные
    private var programExercises: List<ProgramExercise> = emptyList()
    private var exerciseMap: Map<Long, Exercise> = emptyMap()
    private var currentIndex = 0
    private var sessionId: Long = 0
    private var timerJob: Job? = null
    private var elapsedJob: Job? = null
    private var restBetweenSets = 90    // из настроек
    private var restBetweenExercises = 300  // из настроек

    // Запуск тренировки
    fun startWorkout(programId: Long) {
        viewModelScope.launch {
            // Загружаем настройки
            settingsDao.getSettings().collect { settings ->
                if (settings != null) {
                    restBetweenSets = settings.restBetweenSetsSec
                    restBetweenExercises = settings.restBetweenExercisesSec
                }
                return@collect
            }
        }

        viewModelScope.launch {
            // Загружаем упражнения программы
            programExerciseDao.getExercisesForProgram(programId).collect { peList ->
                if (peList.isNotEmpty() && programExercises.isEmpty()) {
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

                    // Создаём сессию в БД
                    sessionId = workoutSessionDao.insertSession(
                        WorkoutSession(
                            programId = programId,
                            startTime = System.currentTimeMillis(),
                            durationSeconds = 0,
                            totalVolumeKg = 0f,
                            exerciseCount = peList.size
                        )
                    )

                    // Показываем первое упражнение
                    showExercise(0)

                    // Запускаем общий таймер
                    startElapsedTimer()
                }
            }
        }
    }

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

    // Нажата кнопка "Подход выполнен"
    fun onSetCompleted() {
        val info = _currentExercise.value ?: return
        val pe = info.programExercise

        // Записываем подход в БД
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

        // Обновляем объём
        _totalVolume.value += pe.weightKg * pe.reps

        val isLastSet = _currentSet.value >= pe.sets
        val isLastExercise = currentIndex >= programExercises.size - 1

        when {
            // Последний подход последнего упражнения — завершаем
            isLastSet && isLastExercise -> {
                _completedExercises.value = programExercises.size
                finishWorkout()
            }
            // Последний подход — отдых перед новым упражнением (5 мин)
            isLastSet -> {
                _completedExercises.value = currentIndex + 1
                startRest(restBetweenExercises)
            }
            // Обычный подход — отдых между подходами (90 сек)
            else -> {
                startRest(restBetweenSets)
            }
        }
    }

    // Запуск таймера отдыха
    private fun startRest(seconds: Int) {
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

    // Продолжить после отдыха
    fun onContinue() {
        timerJob?.cancel()
        val info = _currentExercise.value ?: return
        val isLastSet = _currentSet.value >= info.programExercise.sets

        if (isLastSet) {
            // Переход к следующему упражнению
            showExercise(currentIndex + 1)
        } else {
            // Следующий подход
            _currentSet.value += 1
            _state.value = WorkoutState.EXERCISE
        }
    }

    // Добавить 1 минуту отдыха
    fun onAddMinute() {
        _restSecondsLeft.value += 60
        _state.value = WorkoutState.RESTING
        // Если таймер уже остановился — перезапускаем
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

    // Общий таймер тренировки
    private fun startElapsedTimer() {
        elapsedJob?.cancel()
        elapsedJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    // Завершение тренировки
    private fun finishWorkout() {
        timerJob?.cancel()
        elapsedJob?.cancel()
        _state.value = WorkoutState.COMPLETED

        // Обновляем сессию в БД
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

    // Форматирование времени
    fun formatTime(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    private fun vibrate() {
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
                    longArrayOf(0, 300, 200, 300, 200, 500),  // пауза, вибро, пауза, вибро, пауза, вибро
                    -1  // не повторять
                )
            )
        }
    }
}