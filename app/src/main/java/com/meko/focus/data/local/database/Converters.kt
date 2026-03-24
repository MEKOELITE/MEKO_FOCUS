package com.meko.focus.data.local.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room 数据库类型转换器
 *
 * 用于在 Room 数据库和 Kotlin 类型之间进行转换。
 */
class Converters {

    /**
     * Long 转 Date
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Date 转 Long
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}