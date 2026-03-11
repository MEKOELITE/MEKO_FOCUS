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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDataStore: SettingsDataStore
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideQuoteRepository(): QuoteRepository {
        return QuoteRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideFocusDatabase(
        @ApplicationContext context: Context
    ): FocusDatabase {
        return FocusDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFocusSessionDao(
        database: FocusDatabase
    ): FocusSessionDao {
        return database.focusSessionDao()
    }

    @Provides
    @Singleton
    fun provideFocusSessionRepository(
        focusSessionDao: FocusSessionDao
    ): FocusSessionRepository {
        return FocusSessionRepositoryImpl(focusSessionDao)
    }
}