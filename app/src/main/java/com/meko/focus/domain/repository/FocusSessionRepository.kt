package com.meko.focus.domain.repository

import com.meko.focus.domain.model.FocusSession
import java.util.Date

/**
 * 专注会话数据仓库接口
 *
 * 定义数据持久化的标准操作，采用 Repository 模式抽象数据源。
 */
interface FocusSessionRepository {

    /**
     * 插入新的专注会话
     *
     * @param session 专注会话数据
     * @return 新插入会话的 ID
     * @throws RepositoryException 当数据库操作失败时抛出
     */
    suspend fun insertSession(session: FocusSession): Long

    /**
     * 获取所有专注会话
     *
     * @return 按时间倒序排列的会话列表
     * @throws RepositoryException 当数据库操作失败时抛出
     */
    suspend fun getAllSessions(): List<FocusSession>

    /**
     * 获取指定时间范围内的会话
     *
     * @param start 开始时间（包含）
     * @param end 结束时间（包含）
     * @return 时间范围内的会话列表
     * @throws RepositoryException 当数据库操作失败时抛出
     */
    suspend fun getSessionsByRange(start: Date, end: Date): List<FocusSession>

    /**
     * 获取所有已完成的会话
     *
     * @return 已完成会话列表
     * @throws RepositoryException 当数据库操作失败时抛出
     */
    suspend fun getCompletedSessions(): List<FocusSession>

    /**
     * 清除所有会话记录
     *
     * @throws RepositoryException 当数据库操作失败时抛出
     */
    suspend fun clearAllSessions()
}

/**
 * 数据仓库操作异常
 *
 * 当 Repository 层操作失败时抛出此异常。
 *
 * @property message 错误描述
 * @property cause 原始异常（可选）
 */
class RepositoryException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)