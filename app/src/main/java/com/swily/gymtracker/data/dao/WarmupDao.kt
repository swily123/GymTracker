package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.swily.gymtracker.data.model.Warmup
import kotlinx.coroutines.flow.Flow

@Dao
interface WarmupDao {

    @Query("SELECT * FROM warmups ORDER BY id ASC")
    fun getAllWarmups(): Flow<List<Warmup>>

    @Query("SELECT * FROM warmups WHERE id = :id")
    suspend fun getWarmupById(id: Long): Warmup?

    @Insert
    suspend fun insertWarmup(warmup: Warmup): Long

    @Update
    suspend fun updateWarmup(warmup: Warmup)

    @Delete
    suspend fun deleteWarmup(warmup: Warmup)
}