package com.meko.focus.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meko.focus.data.local.dao.FocusSessionDao
import com.meko.focus.data.local.entity.FocusSessionEntity

/**
 * 专注会话数据库
 *
 * 使用 Room 框架管理本地 SQLite 数据库，提供数据持久化能力。
 * 采用单例模式确保全局只有一个数据库实例。
 */
@Database(
    entities = [FocusSessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusDatabase : RoomDatabase() {

    /** 获取专注会话 DAO */
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        /**
         * 获取数据库单例实例
         *
         * @param context 应用上下文
         * @return 数据库实例
         */
        fun getInstance(context: Context): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_database"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}