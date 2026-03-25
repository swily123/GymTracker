package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "userProfile")
data class UserProfile(
    @PrimaryKey  // AUTO INCREMENT как в SQL
    val id: Int = 1,
    val name: String,
    val heightCm: Int,
    val weightKg: Float,
    val trainingStartDate: Long
)