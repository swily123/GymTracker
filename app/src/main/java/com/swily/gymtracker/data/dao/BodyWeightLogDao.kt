package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swily.gymtracker.data.model.BodyWeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightLogDao {

    @Query("SELECT * FROM bodyWeightLogs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<BodyWeightLog>>

    @Insert
    suspend fun insertLog(log: BodyWeightLog): Long
}