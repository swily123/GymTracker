package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "warmupContents")
data class WarmupContent(
    @PrimaryKey(autoGenerate = true)  // AUTO INCREMENT как в SQL
    val id: Long = 0,
    val warmupId: Long = 0,
    val warmupExerciseId: Long = 0,
    val orderIndex: Int
)