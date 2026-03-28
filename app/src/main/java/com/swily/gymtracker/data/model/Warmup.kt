package com.swily.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warmups")
data class Warmup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)