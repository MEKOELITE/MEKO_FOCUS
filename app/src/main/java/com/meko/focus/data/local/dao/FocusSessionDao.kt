package com.meko.focus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meko.focus.data.local.entity.FocusSessionEntity
import java.util.Date

/**
 * 专注会话数据访问对象 (DAO)
 *
 * 定义与 focus_sessions 表交互的所有数据库操作方法。
 */
@Dao
interface FocusSessionDao {

    /**
     * 插入专注会话
     *
     * @param session 会话实体
     * @return 插入行的 ID
     */
    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    /**
     * 获取所有会话（按时间倒序）
     */
    @Query("SELECT * FROM focus_sessions ORDER BY start_time DESC")
    suspend fun getAllSessions(): List<FocusSessionEntity>

    /**
     * 获取指定时间范围内的会话
     */
    @Query("SELECT * FROM focus_sessions WHERE start_time BETWEEN :start AND :end ORDER BY start_time ASC")
    suspend fun getSessionsByRange(start: Date, end: Date): List<FocusSessionEntity>

    /**
     * 获取所有已完成的会话
     */
    @Query("SELECT * FROM focus_sessions WHERE is_completed = 1 ORDER BY start_time DESC")
    suspend fun getCompletedSessions(): List<FocusSessionEntity>

    /**
     * 清除所有会话记录
     */
    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}