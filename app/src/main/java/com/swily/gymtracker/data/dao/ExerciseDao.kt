package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.swily.gymtracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow

// @Dao — говорит Room "здесь описаны запросы к таблице"
// Аналог SQL запросов, но безопасные и типизированные
@Dao
interface ExerciseDao {

    // Flow — как Observable в Unity, автоматически обновляет UI при изменении данных
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?

    @Insert
    suspend fun insertExercise(exercise: Exercise): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)
}