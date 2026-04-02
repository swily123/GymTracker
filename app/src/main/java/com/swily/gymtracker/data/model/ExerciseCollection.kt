package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exerciseCollections")
data class ExerciseCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)