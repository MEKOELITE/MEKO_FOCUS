package com.meko.focus.domain.repository

import com.meko.focus.domain.model.TimerSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveSettings(settings: TimerSettings)
    fun getSettings(): Flow<TimerSettings>
}