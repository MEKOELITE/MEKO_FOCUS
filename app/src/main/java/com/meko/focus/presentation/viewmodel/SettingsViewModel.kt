package com.meko.focus.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meko.focus.domain.model.TimerSettings
import com.meko.focus.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class SettingsUiState(
        val currentSettings: TimerSettings = TimerSettings(),
        val draftSettings: TimerSettings = TimerSettings(),
        val hasChanges: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().distinctUntilChanged().collect { settings ->
                _uiState.value = SettingsUiState(
                    currentSettings = settings,
                    draftSettings = settings,
                    hasChanges = false,
                    isLoading = false
                )
            }
        }
    }

    fun updateFocusDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(focusDurationMinutes = minutes),
            hasChanges = _uiState.value.draftSettings.copy(focusDurationMinutes = minutes) != _uiState.value.currentSettings
        )
    }

    fun updateShortBreakDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(shortBreakDurationMinutes = minutes),
            hasChanges = _uiState.value.draftSettings.copy(shortBreakDurationMinutes = minutes) != _uiState.value.currentSettings
        )
    }

    fun updateLongBreakDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(longBreakDurationMinutes = minutes),
            hasChanges = _uiState.value.draftSettings.copy(longBreakDurationMinutes = minutes) != _uiState.value.currentSettings
        )
    }

    fun updateAutoSwitch(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(autoSwitch = enabled),
            hasChanges = _uiState.value.draftSettings.copy(autoSwitch = enabled) != _uiState.value.currentSettings
        )
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(vibrationEnabled = enabled),
            hasChanges = _uiState.value.draftSettings.copy(vibrationEnabled = enabled) != _uiState.value.currentSettings
        )
    }

    fun updateSoundEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(soundEnabled = enabled),
            hasChanges = _uiState.value.draftSettings.copy(soundEnabled = enabled) != _uiState.value.currentSettings
        )
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(notificationsEnabled = enabled),
            hasChanges = _uiState.value.draftSettings.copy(notificationsEnabled = enabled) != _uiState.value.currentSettings
        )
    }

    fun updateDarkTheme(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            draftSettings = _uiState.value.draftSettings.copy(darkTheme = enabled),
            hasChanges = _uiState.value.draftSettings.copy(darkTheme = enabled) != _uiState.value.currentSettings
        )
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.saveSettings(_uiState.value.draftSettings)
                _uiState.value = _uiState.value.copy(
                    currentSettings = _uiState.value.draftSettings,
                    hasChanges = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存设置失败: ${e.message}"
                )
            }
        }
    }

    fun resetToDefaults() {
        val defaultSettings = TimerSettings()
        _uiState.value = _uiState.value.copy(
            draftSettings = defaultSettings,
            hasChanges = defaultSettings != _uiState.value.currentSettings
        )
    }
}