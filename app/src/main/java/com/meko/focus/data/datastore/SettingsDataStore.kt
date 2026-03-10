package com.meko.focus.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.meko.focus.domain.model.TimerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore @Inject constructor(
    private val context: Context
) {
    companion object {
        private val FOCUS_DURATION = intPreferencesKey("focus_duration")
        private val SHORT_BREAK_DURATION = intPreferencesKey("short_break_duration")
        private val LONG_BREAK_DURATION = intPreferencesKey("long_break_duration")
        private val AUTO_SWITCH = booleanPreferencesKey("auto_switch")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    suspend fun saveSettings(settings: TimerSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[FOCUS_DURATION] = settings.focusDurationMinutes
            preferences[SHORT_BREAK_DURATION] = settings.shortBreakDurationMinutes
            preferences[LONG_BREAK_DURATION] = settings.longBreakDurationMinutes
            preferences[AUTO_SWITCH] = settings.autoSwitch
            preferences[VIBRATION_ENABLED] = settings.vibrationEnabled
            preferences[SOUND_ENABLED] = settings.soundEnabled
            preferences[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[DARK_THEME] = settings.darkTheme
        }
    }

    fun getSettingsFlow(): Flow<TimerSettings> {
        return context.settingsDataStore.data.map { preferences ->
            TimerSettings(
                focusDurationMinutes = preferences[FOCUS_DURATION] ?: 25,
                shortBreakDurationMinutes = preferences[SHORT_BREAK_DURATION] ?: 5,
                longBreakDurationMinutes = preferences[LONG_BREAK_DURATION] ?: 15,
                autoSwitch = preferences[AUTO_SWITCH] ?: true,
                vibrationEnabled = preferences[VIBRATION_ENABLED] ?: true,
                soundEnabled = preferences[SOUND_ENABLED] ?: true,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                darkTheme = preferences[DARK_THEME] ?: false
            )
        }
    }
}