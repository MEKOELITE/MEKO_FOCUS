package com.meko.focus.data.local.mapper

import com.meko.focus.data.local.entity.FocusSessionEntity
import com.meko.focus.domain.model.FocusSession

fun FocusSessionEntity.toDomain(): FocusSession {
    return FocusSession(
        id = id,
        startTime = startTime,
        duration = duration,
        isCompleted = isCompleted,
        tag = tag
    )
}

fun FocusSession.toEntity(): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        startTime = startTime,
        duration = duration,
        isCompleted = isCompleted,
        tag = tag
    )
}