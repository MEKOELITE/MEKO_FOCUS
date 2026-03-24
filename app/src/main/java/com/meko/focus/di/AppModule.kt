package com.meko.focus.di

import android.content.Context
import com.meko.focus.data.datastore.SettingsDataStore
import com.meko.focus.data.local.dao.FocusSessionDao
import com.meko.focus.data.local.database.FocusDatabase
import com.meko.focus.data.repository.FocusSessionRepositoryImpl
import com.meko.focus.data.repository.QuoteRepositoryImpl
import com.meko.focus.data.repository.SettingsRepositoryImpl
import com.meko.focus.domain.repository.FocusSessionRepository
import com.meko.focus.domain.repository.QuoteRepository
import com.meko.focus.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块
 *
 * 提供应用级别的单例依赖，确保整个应用共享同一实例。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供设置数据存储实例
     */
    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }

    /**
     * 提供设置 Repository
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDataStore: SettingsDataStore
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    /**
     * 提供名言 Repository
     */
    @Provides
    @Singleton
    fun provideQuoteRepository(): QuoteRepository {
        return QuoteRepositoryImpl()
    }

    /**
     * 提供数据库实例
     *
     * 使用单例模式确保整个应用只有一个数据库实例。
     */
    @Provides
    @Singleton
    fun provideFocusDatabase(
        @ApplicationContext context: Context
    ): FocusDatabase {
        return FocusDatabase.getInstance(context)
    }

    /**
     * 提供专注会话 DAO
     */
    @Provides
    @Singleton
    fun provideFocusSessionDao(
        database: FocusDatabase
    ): FocusSessionDao {
        return database.focusSessionDao()
    }

    /**
     * 提供专注会话 Repository
     */
    @Provides
    @Singleton
    fun provideFocusSessionRepository(
        focusSessionDao: FocusSessionDao
    ): FocusSessionRepository {
        return FocusSessionRepositoryImpl(focusSessionDao)
    }
}