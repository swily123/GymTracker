package com.swily.gymtracker

import android.content.Context
import com.swily.gymtracker.data.GymDatabase
import com.swily.gymtracker.data.model.Settings
import kotlinx.coroutines.flow.Flow

object SettingsProvider {
    private var database: GymDatabase? = null

    fun init(context: Context) {
        database = GymDatabase.getDatabase(context)
    }

    fun getSettings(): Flow<Settings?> {
        return database!!.settingsDao().getSettings()
    }
}