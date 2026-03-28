package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity — говорит Room "создай таблицу с таким именем"
// Аналог CREATE TABLE exercises (...)
@Entity(tableName = "program")
data class Program(
    @PrimaryKey(autoGenerate = true)  // AUTO INCREMENT как в SQL
    val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String,
    val estimatedMinutes: Int,
    val warmupId: Long? = null
)