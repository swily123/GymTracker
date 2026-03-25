package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swily.gymtracker.data.model.ProgramExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramExerciseDao {

    @Query("SELECT * FROM programsExercises WHERE programId = :programId ORDER BY orderIndex ASC")
    fun getExercisesForProgram(programId: Long): Flow<List<ProgramExercise>>

    @Insert
    suspend fun insertAll(exercises: List<ProgramExercise>)

    @Query("DELETE FROM programsExercises WHERE programId = :programId")
    suspend fun deleteByProgramId(programId: Long)
}