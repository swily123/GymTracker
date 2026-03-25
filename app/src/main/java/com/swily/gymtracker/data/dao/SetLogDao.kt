package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swily.gymtracker.data.model.SetLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SetLogDao {

    @Query("SELECT * FROM setLog WHERE sessionId = :sessionId")
    fun getLogsForSession(sessionId: Long): Flow<List<SetLog>>

    @Query("SELECT * FROM setLog WHERE exerciseId = :exerciseId ORDER BY completedAt DESC")
    fun getLogsForExercise(exerciseId: Long): Flow<List<SetLog>>

    @Insert
    suspend fun insertSetLog(setLog: SetLog): Long
}