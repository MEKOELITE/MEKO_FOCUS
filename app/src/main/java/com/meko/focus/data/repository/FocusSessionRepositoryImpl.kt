package com.meko.focus.data.repository

import com.meko.focus.data.local.dao.FocusSessionDao
import com.meko.focus.data.local.database.FocusDatabase
import com.meko.focus.data.local.mapper.toDomain
import com.meko.focus.data.local.mapper.toEntity
import com.meko.focus.domain.model.FocusSession
import com.meko.focus.domain.repository.FocusSessionRepository
import java.util.Date
import javax.inject.Inject

class FocusSessionRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : FocusSessionRepository {

    override suspend fun insertSession(session: FocusSession): Long {
        return focusSessionDao.insertSession(session.toEntity())
    }

    override suspend fun getAllSessions(): List<FocusSession> {
        return focusSessionDao.getAllSessions().map { it.toDomain() }
    }

    override suspend fun getSessionsByRange(start: Date, end: Date): List<FocusSession> {
        return focusSessionDao.getSessionsByRange(start, end).map { it.toDomain() }
    }

    override suspend fun getCompletedSessions(): List<FocusSession> {
        return focusSessionDao.getCompletedSessions().map { it.toDomain() }
    }

    override suspend fun clearAllSessions() {
        focusSessionDao.clearAllSessions()
    }
}