package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swily.gymtracker.data.model.WarmupContent
import com.swily.gymtracker.data.model.WarmupExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WarmupContentDao {

    @Query("""
        SELECT we.* FROM warmupExercises we 
        INNER JOIN warmupContents wc ON we.id = wc.warmupExerciseId 
        WHERE wc.warmupId = :warmupId 
        ORDER BY wc.orderIndex ASC
    """)
    fun getExercisesForWarmup(warmupId: Long): Flow<List<WarmupExercise>>

    @Query("SELECT * FROM warmupContents WHERE warmupId = :warmupId ORDER BY orderIndex ASC")
    fun getContentsForWarmup(warmupId: Long): Flow<List<WarmupContent>>

    @Insert
    suspend fun insertAll(contents: List<WarmupContent>)

    @Query("DELETE FROM warmupContents WHERE warmupId = :warmupId")
    suspend fun deleteByWarmupId(warmupId: Long)
}