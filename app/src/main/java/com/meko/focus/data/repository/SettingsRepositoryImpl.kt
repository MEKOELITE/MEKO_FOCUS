package com.meko.focus.data.repository

import com.meko.focus.data.datastore.SettingsDataStore
import com.meko.focus.domain.model.TimerSettings
import com.meko.focus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override suspend fun saveSettings(settings: TimerSettings) {
        settingsDataStore.saveSettings(settings)
    }

    override fun getSettings(): Flow<TimerSettings> {
        return settingsDataStore.getSettingsFlow()
    }
}