package com.swily.gymtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.swily.gymtracker.data.dao.*
import com.swily.gymtracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Перечисляем ВСЕ таблицы и версию базы
@Database(
    entities = [
        Exercise::class,
        Program::class,
        ProgramExercise::class,
        WorkoutSession::class,
        SetLog::class,
        UserProfile::class,
        BodyWeightLog::class,
        Settings::class,
        Warmup::class,
        WarmupContent::class,
        WarmupExercise::class,
        ExerciseCollection::class,
        WarmupExerciseCollection::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {

    // Room сам реализует эти методы — мы только объявляем
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao
    abstract fun programExerciseDao(): ProgramExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun setLogDao(): SetLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyWeightLogDao(): BodyWeightLogDao
    abstract fun settingsDao(): SettingsDao

    abstract fun warmupDao(): WarmupDao
    abstract fun warmupContentDao(): WarmupContentDao
    abstract fun warmupExerciseDao(): WarmupExerciseDao
    abstract fun exerciseCollectionDao(): ExerciseCollectionDao
    abstract fun warmupExerciseCollectionDao(): WarmupExerciseCollectionDao

    companion object {
        // Singleton — одна база на всё приложение
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            // Если база уже создана — вернуть её
            // Если нет — создать (synchronized чтобы не создать дважды)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_tracker_database"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Предзаполнение базы при первом запуске
    private class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateDatabase(database)
                }
            }
        }
    }
}

// Заполняем базу начальными данными
// TODO: Настроить упражнения, веса и советы под свои реальные тренировки
private suspend fun prepopulateDatabase(database: GymDatabase) {
    val exerciseDao = database.exerciseDao()
    val programDao = database.programDao()
    val programExerciseDao = database.programExerciseDao()
    val settingsDao = database.settingsDao()
    val userProfileDao = database.userProfileDao()

    // --- Упражнения ---
    val exercises = listOf(
        Exercise(name = "Жим штанги лёжа", defaultReps = 12, defaultWeightKg = 60f, muscleGroup = "Грудь", tip = "Опускай штангу к середине груди, локти под 45°"),
        Exercise(name = "Жим гантелей наклон", defaultReps = 12, defaultWeightKg = 20f, muscleGroup = "Грудь", tip = "Угол скамьи 30-45°, не разводи локти слишком широко"),
        Exercise(name = "Разведение гантелей", defaultReps = 12, defaultWeightKg = 14f, muscleGroup = "Грудь", tip = "Слегка согни локти, чувствуй растяжение в груди"),
        Exercise(name = "Тяга верхнего блока", defaultReps = 12, defaultWeightKg = 50f, muscleGroup = "Спина", tip = "Тяни к верху груди, своди лопатки"),
        Exercise(name = "Тяга гантели в наклоне", defaultReps = 12, defaultWeightKg = 22f, muscleGroup = "Спина", tip = "Спина параллельна полу, тяни к поясу"),
        Exercise(name = "Жим гантелей сидя", defaultReps = 12, defaultWeightKg = 16f, muscleGroup = "Плечи", tip = "Не выпрямляй руки полностью в верхней точке"),
        Exercise(name = "Подъём на бицепс", defaultReps = 12, defaultWeightKg = 12f, muscleGroup = "Руки", tip = "Не раскачивай корпус, работай только бицепсом"),
        Exercise(name = "Французский жим", defaultReps = 12, defaultWeightKg = 20f, muscleGroup = "Руки", tip = "Локти смотрят в потолок, не разводи их"),
        Exercise(name = "Приседания со штангой", defaultReps = 12, defaultWeightKg = 60f, muscleGroup = "Ноги", tip = "Колени по направлению носков, спина прямая"),
        Exercise(name = "Жим ногами", defaultReps = 12, defaultWeightKg = 100f, muscleGroup = "Ноги", tip = "Не выпрямляй колени полностью"),
        Exercise(name = "Выпады с гантелями", defaultReps = 12, defaultWeightKg = 14f, muscleGroup = "Ноги", tip = "Шаг достаточно широкий, колено не выходит за носок"),
        Exercise(name = "Сгибание ног в тренажёре", defaultReps = 12, defaultWeightKg = 40f, muscleGroup = "Ноги", tip = "Плавное движение, задержись в верхней точке"),
        Exercise(name = "Подъём на носки", defaultReps = 15, defaultWeightKg = 40f, muscleGroup = "Икры", tip = "Полная амплитуда: растяни внизу, сожми вверху")
    )
    val exerciseIds = exercises.map { exerciseDao.insertExercise(it) }

    // --- Программы ---
    val fullBodyId = programDao.insertProgram(
        Program(name = "Full Body", description = "Всё тело за одну сессию", colorHex = "#E8593C", estimatedMinutes = 60)
    )
    val upperBodyId = programDao.insertProgram(
        Program(name = "Upper Body", description = "Грудь, спина, плечи, руки", colorHex = "#3B82F6", estimatedMinutes = 45)
    )
    val lowerBodyId = programDao.insertProgram(
        Program(name = "Lower Body", description = "Ноги, ягодицы, икры", colorHex = "#8B5CF6", estimatedMinutes = 50)
    )

    // --- Связки программа-упражнения ---
    // Upper Body: первые 8 упражнений
    val upperBodyExercises = exerciseIds.take(8).mapIndexed { index, exerciseId ->
        ProgramExercise(programId = upperBodyId, exerciseId = exerciseId, orderIndex = index, reps = 12, weightKg = exercises[index].defaultWeightKg)
    }
    programExerciseDao.insertAll(upperBodyExercises)

    // Lower Body: упражнения 8-12 (ноги + икры)
    val lowerBodyExercises = exerciseIds.drop(8).mapIndexed { index, exerciseId ->
        ProgramExercise(programId = lowerBodyId, exerciseId = exerciseId, orderIndex = index, reps = exercises[index + 8].defaultReps, weightKg = exercises[index + 8].defaultWeightKg)
    }
    programExerciseDao.insertAll(lowerBodyExercises)

    // Full Body: микс из всех групп
    val fullBodyIndices = listOf(0, 3, 5, 6, 8, 10, 12) // жим, тяга, жим сидя, бицепс, присед, выпады, носки
    val fullBodyExercises = fullBodyIndices.mapIndexed { index, exIndex ->
        ProgramExercise(programId = fullBodyId, exerciseId = exerciseIds[exIndex], orderIndex = index, reps = exercises[exIndex].defaultReps, weightKg = exercises[exIndex].defaultWeightKg)
    }
    programExerciseDao.insertAll(fullBodyExercises)

    // --- Настройки по умолчанию ---
    settingsDao.insertSettings(Settings())

    // --- Профиль ---
    userProfileDao.insertProfile(
        UserProfile(name = "Alex", heightCm = 178, weightKg = 78.2f, trainingStartDate = System.currentTimeMillis())
    )
}