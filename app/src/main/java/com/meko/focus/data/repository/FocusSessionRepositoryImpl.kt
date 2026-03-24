package com.meko.focus.data.repository

import com.meko.focus.data.local.dao.FocusSessionDao
import com.meko.focus.data.local.mapper.toDomain
import com.meko.focus.data.local.mapper.toEntity
import com.meko.focus.domain.model.FocusSession
import com.meko.focus.domain.repository.FocusSessionRepository
import com.meko.focus.domain.repository.RepositoryException
import java.util.Date
import javax.inject.Inject

/**
 * 专注会话 Repository 实现类
 *
 * 负责将 Domain 层请求转换为数据层操作，处理数据库异常并转换为统一格式。
 *
 * @property focusSessionDao 专注会话数据访问对象
 */
class FocusSessionRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : FocusSessionRepository {

    override suspend fun insertSession(session: FocusSession): Long {
        return try {
            focusSessionDao.insertSession(session.toEntity())
        } catch (e: Exception) {
            throw RepositoryException(
                message = "保存专注会话失败: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun getAllSessions(): List<FocusSession> {
        return try {
            focusSessionDao.getAllSessions().map { it.toDomain() }
        } catch (e: Exception) {
            throw RepositoryException(
                message = "获取所有会话失败: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun getSessionsByRange(start: Date, end: Date): List<FocusSession> {
        return try {
            focusSessionDao.getSessionsByRange(start, end).map { it.toDomain() }
        } catch (e: Exception) {
            throw RepositoryException(
                message = "获取时间范围内会话失败: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun getCompletedSessions(): List<FocusSession> {
        return try {
            focusSessionDao.getCompletedSessions().map { it.toDomain() }
        } catch (e: Exception) {
            throw RepositoryException(
                message = "获取已完成会话失败: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun clearAllSessions() {
        try {
            focusSessionDao.clearAllSessions()
        } catch (e: Exception) {
            throw RepositoryException(
                message = "清除会话记录失败: ${e.message}",
                cause = e
            )
        }
    }
}