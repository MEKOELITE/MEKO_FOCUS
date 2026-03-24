package com.meko.focus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 专注会话数据库实体
 *
 * 对应数据库中的 focus_sessions 表，用于 Room 持久化存储。
 *
 * @property id 主键，自增
 * @property startTime 会话开始时间
 * @property duration 会话持续时长（毫秒）
 * @property isCompleted 是否已完成
 * @property tag 会话标签
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Date,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "tag")
    val tag: String? = null
)