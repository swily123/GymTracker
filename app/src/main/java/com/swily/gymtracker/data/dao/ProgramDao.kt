package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.swily.gymtracker.data.model.Program
import kotlinx.coroutines.flow.Flow

// @Dao — говорит Room "здесь описаны запросы к таблице"
// Аналог SQL запросов, но безопасные и типизированные
@Dao
interface ProgramDao {

    // Flow — как Observable в Unity, автоматически обновляет UI при изменении данных
    @Query("SELECT * FROM program ORDER BY name ASC")
    fun getAllPrograms(): Flow<List<Program>>

    @Query("SELECT * FROM program WHERE id = :id")
    suspend fun getProgramById(id: Long): Program?

    @Insert
    suspend fun insertProgram(programs: Program): Long

    @Update
    suspend fun updateProgram(programs: Program)

    @Delete
    suspend fun deleteProgram(programs: Program)
}