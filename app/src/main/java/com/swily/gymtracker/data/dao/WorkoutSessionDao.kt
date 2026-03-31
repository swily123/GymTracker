package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.swily.gymtracker.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Query("SELECT * FROM workoutSessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workoutSessions ORDER BY startTime DESC LIMIT 1")
    fun getLastSession(): Flow<WorkoutSession?>

    @Query("DELETE FROM workoutSessions WHERE endTime IS NULL")
    suspend fun deleteUnfinishedSessions()

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)
}