package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "setLog")
data class SetLog(
    @PrimaryKey(autoGenerate = true)  // AUTO INCREMENT как в SQL
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseId: Long = 0,
    val exerciseIndex: Int,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val completedAt: Long
)