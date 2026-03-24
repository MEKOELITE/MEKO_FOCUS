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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val persistMutex = Mutex()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().distinctUntilChanged().collect { settings ->
                val cur = _uiState.value
                if (cur.hasChanges) {
                    _uiState.value = cur.copy(
                        currentSettings = settings,
                        hasChanges = cur.draftSettings != settings
                    )
                } else {
                    _uiState.value = SettingsUiState(
                        currentSettings = settings,
                        draftSettings = settings,
                        hasChanges = false,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun schedulePersist() {
        viewModelScope.launch {
            persistIfNeeded()
        }
    }

    private suspend fun persistIfNeeded() {
        persistMutex.withLock {
            val snapshot = _uiState.value
            if (snapshot.draftSettings == snapshot.currentSettings) return@withLock
            try {
                settingsRepository.saveSettings(snapshot.draftSettings)
                _uiState.value = _uiState.value.copy(
                    currentSettings = snapshot.draftSettings,
                    hasChanges = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存设置失败: ${e.message}"
                )
            }
        }
    }

    fun updateFocusDuration(minutes: Int) {
        val clamped = minutes.coerceIn(
            TimerSettings.MIN_FOCUS_DURATION,
            TimerSettings.MAX_FOCUS_DURATION
        )
        val cur = _uiState.value
        val newDraft = cur.draftSettings.copy(focusDurationMinutes = clamped)
        _uiState.value = cur.copy(
            draftSettings = newDraft,
            hasChanges = newDraft != cur.currentSettings
        )
        schedulePersist()
    }

    fun updateShortBreakDuration(minutes: Int) {
        val clamped = minutes.coerceIn(
            TimerSettings.MIN_SHORT_BREAK,
            TimerSettings.MAX_SHORT_BREAK
        )
        val cur = _uiState.value
        val newDraft = cur.draftSettings.copy(shortBreakDurationMinutes = clamped)
        _uiState.value = cur.copy(
            draftSettings = newDraft,
            hasChanges = newDraft != cur.currentSettings
        )
        schedulePersist()
    }

    fun updateLongBreakDuration(minutes: Int) {
        val clamped = minutes.coerceIn(
            TimerSettings.MIN_LONG_BREAK,
            TimerSettings.MAX_LONG_BREAK
        )
        val cur = _uiState.value
        val newDraft = cur.draftSettings.copy(longBreakDurationMinutes = clamped)
        _uiState.value = cur.copy(
            draftSettings = newDraft,
            hasChanges = newDraft != cur.currentSettings
        )
        schedulePersist()
    }

    fun updateAutoSwitch(enabled: Boolean) {
        val newDraftSettings = _uiState.value.draftSettings.copy(autoSwitch = enabled)
        _uiState.value = _uiState.value.copy(
            draftSettings = newDraftSettings,
            hasChanges = newDraftSettings != _uiState.value.currentSettings
        )
        schedulePersist()
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        val newDraftSettings = _uiState.value.draftSettings.copy(vibrationEnabled = enabled)
        _uiState.value = _uiState.value.copy(
            draftSettings = newDraftSettings,
            hasChanges = newDraftSettings != _uiState.value.currentSettings
        )
        schedulePersist()
    }

    fun updateSoundEnabled(enabled: Boolean) {
        val newDraftSettings = _uiState.value.draftSettings.copy(soundEnabled = enabled)
        _uiState.value = _uiState.value.copy(
            draftSettings = newDraftSettings,
            hasChanges = newDraftSettings != _uiState.value.currentSettings
        )
        schedulePersist()
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val newDraftSettings = _uiState.value.draftSettings.copy(notificationsEnabled = enabled)
        _uiState.value = _uiState.value.copy(
            draftSettings = newDraftSettings,
            hasChanges = newDraftSettings != _uiState.value.currentSettings
        )
        schedulePersist()
    }

    fun updateDarkTheme(enabled: Boolean) {
        val newDraftSettings = _uiState.value.draftSettings.copy(darkTheme = enabled)
        _uiState.value = _uiState.value.copy(
            draftSettings = newDraftSettings,
            hasChanges = newDraftSettings != _uiState.value.currentSettings
        )
        schedulePersist()
    }

    fun saveSettings() {
        viewModelScope.launch {
            persistIfNeeded()
        }
    }

    /**
     * 在离开设置页前写入 DataStore，避免用户未点保存就返回导致修改丢失。
     */
    fun persistAndRun(onFinished: () -> Unit) {
        viewModelScope.launch {
            persistIfNeeded()
            onFinished()
        }
    }

    fun resetToDefaults() {
        val defaultSettings = TimerSettings()
        val cur = _uiState.value
        _uiState.value = cur.copy(
            draftSettings = defaultSettings,
            hasChanges = defaultSettings != cur.currentSettings
        )
        schedulePersist()
    }
}