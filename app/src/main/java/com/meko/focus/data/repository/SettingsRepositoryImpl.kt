package com.meko.focus.data.repository

import com.meko.focus.data.datastore.SettingsDataStore
import com.meko.focus.domain.model.TimerSettings
import com.meko.focus.domain.repository.RepositoryException
import com.meko.focus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 设置 Repository 实现类
 *
 * @property settingsDataStore 设置数据存储
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override suspend fun saveSettings(settings: TimerSettings) {
        try {
            settingsDataStore.saveSettings(settings)
        } catch (e: Exception) {
            throw RepositoryException(
                message = "保存设置失败: ${e.message}",
                cause = e
            )
        }
    }

    override fun getSettings(): Flow<TimerSettings> {
        return settingsDataStore.getSettingsFlow()
    }
}