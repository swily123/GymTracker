package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.swily.gymtracker.data.model.WarmupExerciseCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface WarmupExerciseCollectionDao {

    @Query("SELECT * FROM warmupExerciseCollections ORDER BY id ASC")
    fun getAllCollections(): Flow<List<WarmupExerciseCollection>>

    @Insert
    suspend fun insertCollection(collection: WarmupExerciseCollection): Long

    @Update
    suspend fun updateCollection(collection: WarmupExerciseCollection)

    @Delete
    suspend fun deleteCollection(collection: WarmupExerciseCollection)
}