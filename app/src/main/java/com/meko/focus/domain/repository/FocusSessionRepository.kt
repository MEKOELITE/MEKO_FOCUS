package com.meko.focus.domain.repository

import com.meko.focus.domain.model.FocusSession
import java.util.Date

interface FocusSessionRepository {
    suspend fun insertSession(session: FocusSession): Long
    suspend fun getAllSessions(): List<FocusSession>
    suspend fun getSessionsByRange(start: Date, end: Date): List<FocusSession>
    suspend fun getCompletedSessions(): List<FocusSession>
    suspend fun clearAllSessions()
}