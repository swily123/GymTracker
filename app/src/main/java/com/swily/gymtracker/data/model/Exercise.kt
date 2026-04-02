package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)  // AUTO INCREMENT как в SQL
    val id: Long = 0,
    val name: String,
    val defaultReps: Int,
    val defaultWeightKg: Float,
    val muscleGroup: String = "",
    val tip: String = "",
    val collectionId: Long? = null
)