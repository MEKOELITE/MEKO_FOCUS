package com.meko.focus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meko.focus.data.local.entity.FocusSessionEntity
import java.util.Date

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions ORDER BY start_time DESC")
    suspend fun getAllSessions(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE start_time BETWEEN :start AND :end ORDER BY start_time ASC")
    suspend fun getSessionsByRange(start: Date, end: Date): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE is_completed = 1 ORDER BY start_time DESC")
    suspend fun getCompletedSessions(): List<FocusSessionEntity>

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}