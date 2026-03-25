package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "programsExercises")
data class ProgramExercise(
    @PrimaryKey(autoGenerate = true)  // AUTO INCREMENT как в SQL
    val id: Long = 0,
    val programId: Long = 0,
    val exerciseId: Long = 0,
    val orderIndex: Int,
    val sets: Int = 3,
    val reps: Int = 12,
    val weightKg: Float,
    val isWarmup: Boolean = false
)