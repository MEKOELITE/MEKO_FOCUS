package com.meko.focus.di

import android.content.Context
import com.meko.focus.data.datastore.SettingsDataStore
import com.meko.focus.data.repository.QuoteRepositoryImpl
import com.meko.focus.data.repository.SettingsRepositoryImpl
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
}