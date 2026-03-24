package com.meko.focus.domain.model

import java.util.Date

/**
 * 专注会话数据模型
 *
 * @property id 会话唯一标识，0 表示新会话
 * @property startTime 会话开始时间
 * @property duration 会话持续时长（毫秒）
 * @property isCompleted 是否已完成
 * @property tag 会话标签（如"工作"、"学习"等）
 */
data class FocusSession(
    val id: Long = 0,
    val startTime: Date,
    val duration: Long,
    val isCompleted: Boolean,
    val tag: String? = null
) {
    /** 会话持续分钟数 */
    val durationMinutes: Int
        get() = (duration / (60 * 1000)).toInt()
}