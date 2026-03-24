package com.meko.focus.data.local.mapper

import com.meko.focus.data.local.entity.FocusSessionEntity
import com.meko.focus.domain.model.FocusSession

/**
 * 专注会话实体与领域模型转换函数
 */

/**
 * 将数据库实体转换为领域模型
 *
 * @receiver FocusSessionEntity 数据库实体
 * @return FocusSession 领域模型
 */
fun FocusSessionEntity.toDomain(): FocusSession {
    return FocusSession(
        id = id,
        startTime = startTime,
        duration = duration,
        isCompleted = isCompleted,
        tag = tag
    )
}

/**
 * 将领域模型转换为数据库实体
 *
 * @receiver FocusSession 领域模型
 * @return FocusSessionEntity 数据库实体
 */
fun FocusSession.toEntity(): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        startTime = startTime,
        duration = duration,
        isCompleted = isCompleted,
        tag = tag
    )
}