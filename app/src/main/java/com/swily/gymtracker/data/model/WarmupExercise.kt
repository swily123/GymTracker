package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warmupExercises")
data class WarmupExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val reps: Int = 4,
    val collectionId: Long? = null
)