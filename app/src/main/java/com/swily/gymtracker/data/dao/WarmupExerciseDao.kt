package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.swily.gymtracker.data.model.WarmupExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WarmupExerciseDao {

    @Query("SELECT * FROM warmupExercises ORDER BY id ASC")
    fun getAllWarmupExercises(): Flow<List<WarmupExercise>>

    @Query("SELECT * FROM warmupExercises WHERE id = :id")
    suspend fun getWarmupExerciseById(id: Long): WarmupExercise?

    @Insert
    suspend fun insertWarmupExercise(exercise: WarmupExercise): Long

    @Update
    suspend fun updateWarmupExercise(exercise: WarmupExercise)

    @Delete
    suspend fun deleteWarmupExercise(exercise: WarmupExercise)
}