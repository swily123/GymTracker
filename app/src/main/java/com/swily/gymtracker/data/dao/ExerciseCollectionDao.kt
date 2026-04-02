package com.swily.gymtracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.swily.gymtracker.data.model.ExerciseCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseCollectionDao {

    @Query("SELECT * FROM exerciseCollections ORDER BY id ASC")
    fun getAllCollections(): Flow<List<ExerciseCollection>>

    @Insert
    suspend fun insertCollection(collection: ExerciseCollection): Long

    @Update
    suspend fun updateCollection(collection: ExerciseCollection)

    @Delete
    suspend fun deleteCollection(collection: ExerciseCollection)
}