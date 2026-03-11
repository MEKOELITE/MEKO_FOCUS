package com.meko.focus.domain.model

import java.util.Date

data class FocusSession(
    val id: Long = 0,
    val startTime: Date,
    val duration: Long, // 单位：毫秒
    val isCompleted: Boolean,
    val tag: String? = null
) {
    val durationMinutes: Int
        get() = (duration / (60 * 1000)).toInt()
}