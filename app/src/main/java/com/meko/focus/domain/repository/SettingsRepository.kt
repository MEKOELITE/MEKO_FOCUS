package com.meko.focus.domain.repository

import com.meko.focus.domain.model.TimerSettings
import kotlinx.coroutines.flow.Flow

/**
 * 设置数据仓库接口
 *
 * 负责管理番茄钟应用的用户偏好设置。
 */
interface SettingsRepository {

    /**
     * 保存设置
     *
     * @param settings 计时器设置数据
     * @throws RepositoryException 当保存操作失败时抛出
     */
    suspend fun saveSettings(settings: TimerSettings)

    /**
     * 获取设置流
     *
     * @return 包含最新设置的 Flow，每次设置变更都会发出新值
     */
    fun getSettings(): Flow<TimerSettings>
}