package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey  // AUTO INCREMENT как в SQL
    val id: Int = 1,
    val restBetweenSetsSec: Int = 180,
    val restBetweenExercisesSec: Int = 300,
    val vibrationEnabled: Boolean = true,
    val useKg: Boolean = true
)