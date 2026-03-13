package com.meko.focus.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meko.focus.domain.model.FocusSession
import com.meko.focus.domain.model.SessionType
import com.meko.focus.domain.model.TimerSettings
import com.meko.focus.domain.model.TimerState
import com.meko.focus.domain.repository.FocusSessionRepository
import com.meko.focus.domain.repository.SettingsRepository
import com.meko.focus.presentation.component.PomodoroSegment
import com.meko.focus.service.TimerForegroundService
import com.meko.focus.util.SoundHelper
import com.meko.focus.util.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusSessionRepository: FocusSessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Service binding
    private var timerService: TimerForegroundService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.TimerBinder
            timerService = binder.getService()
            serviceBound = true

            // Observe service state
            viewModelScope.launch {
                timerService?.remainingTimeMs?.collect { time ->
                    _uiState.value = _uiState.value.copy(remainingTimeMs = time)
                }
            }
            viewModelScope.launch {
                timerService?.isRunning?.collect { running ->
                    if (running && _uiState.value.timerState != TimerState.RUNNING) {
                        _uiState.value = _uiState.value.copy(timerState = TimerState.RUNNING)
                    } else if (!running && _uiState.value.timerState == TimerState.RUNNING) {
                        _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
                    }
                }
            }

            // Set timer finished callback
            timerService?.setOnTimerFinishedListener {
                viewModelScope.launch {
                    onTimerFinished()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            serviceBound = false
        }
    }

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().distinctUntilChanged().collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
                if (_uiState.value.timerState == TimerState.STOPPED &&
                    _uiState.value.remainingTimeMs == 25L * 60 * 1000) {
                    _uiState.value = _uiState.value.copy(
                        remainingTimeMs = getDefaultTimeForSegment(_uiState.value.selectedSegment)
                    )
                }
            }
        }

        // Bind to service
        bindService()
    }

    private fun bindService() {
        val intent = Intent(context, TimerForegroundService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    // Timer job for local countdown (when service not running)
    private var timerJob: Job? = null

    fun toggleTimer() {
        when (_uiState.value.timerState) {
            TimerState.STOPPED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
            TimerState.PAUSED -> resumeTimer()
        }
    }

    fun selectSegment(segment: PomodoroSegment) {
        stopTimer()
        _uiState.value = _uiState.value.copy(
            selectedSegment = segment,
            sessionStartTime = null,
            remainingTimeMs = getDefaultTimeForSegment(segment)
        )
    }

    fun resetTimer() {
        stopTimer()
        _uiState.value = _uiState.value.copy(
            remainingTimeMs = getDefaultTimeForSegment(_uiState.value.selectedSegment),
            timerState = TimerState.STOPPED,
            sessionStartTime = null
        )
    }

    private fun startTimer() {
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.RUNNING,
            sessionStartTime = Date()
        )

        // Start foreground service
        TimerForegroundService.startTimer(
            context,
            _uiState.value.remainingTimeMs,
            _uiState.value.selectedSegment.name
        )

        // Also start local countdown as backup (for UI updates)
        startLocalCountdown()
    }

    private fun pauseTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
        TimerForegroundService.pauseTimer(context)
        stopLocalCountdown()
    }

    private fun resumeTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.RUNNING)
        TimerForegroundService.resumeTimer(context)
        startLocalCountdown()
    }

    private fun stopTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.STOPPED)
        TimerForegroundService.stopTimer(context)
        stopLocalCountdown()
    }

    private fun startLocalCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerState == TimerState.RUNNING && _uiState.value.remainingTimeMs > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(remainingTimeMs = _uiState.value.remainingTimeMs - 1000)
            }
            if (_uiState.value.remainingTimeMs <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun stopLocalCountdown() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun onTimerFinished() {
        val currentState = _uiState.value
        val settings = currentState.settings

        // Trigger vibration
        if (settings.vibrationEnabled) {
            VibrationHelper.vibrateTimerComplete(context)
        }

        // Play sound
        if (settings.soundEnabled) {
            SoundHelper.initialize(context)
            SoundHelper.playComplete()
        }

        if (settings.autoSwitch) {
            when (currentState.selectedSegment) {
                PomodoroSegment.FOCUS -> {
                    val newCompletedSessions = currentState.completedFocusSessions + 1
                    val isLongBreak = newCompletedSessions % 4 == 0

                    saveFocusSession(currentState)

                    _uiState.value = currentState.copy(
                        selectedSegment = PomodoroSegment.BREAK,
                        completedFocusSessions = newCompletedSessions,
                        remainingTimeMs = if (isLongBreak) {
                            getLongBreakDuration()
                        } else {
                            getShortBreakDuration()
                        },
                        timerState = TimerState.STOPPED,
                        sessionStartTime = null
                    )
                }
                PomodoroSegment.BREAK -> {
                    _uiState.value = currentState.copy(
                        selectedSegment = PomodoroSegment.FOCUS,
                        remainingTimeMs = getFocusDuration(),
                        timerState = TimerState.STOPPED,
                        sessionStartTime = null
                    )
                }
            }
        } else {
            _uiState.value = currentState.copy(
                timerState = TimerState.STOPPED,
                sessionStartTime = null
            )

            if (currentState.selectedSegment == PomodoroSegment.FOCUS) {
                saveFocusSession(currentState)
                val newCompletedSessions = currentState.completedFocusSessions + 1
                _uiState.value = _uiState.value.copy(
                    completedFocusSessions = newCompletedSessions
                )
            }
        }

        // Stop the service
        TimerForegroundService.stopTimer(context)
    }

    private fun saveFocusSession(state: TimerUiState) {
        val startTime = state.sessionStartTime ?: return

        viewModelScope.launch {
            try {
                val session = FocusSession(
                    startTime = startTime,
                    duration = getFocusDuration() - state.remainingTimeMs,
                    isCompleted = true,
                    tag = "专注"
                )
                focusSessionRepository.insertSession(session)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getDefaultTimeForSegment(segment: PomodoroSegment): Long {
        return when (segment) {
            PomodoroSegment.FOCUS -> getFocusDuration()
            PomodoroSegment.BREAK -> getShortBreakDuration()
        }
    }

    private fun getFocusDuration(): Long = _uiState.value.settings.focusDurationMinutes * 60 * 1000L
    private fun getShortBreakDuration(): Long = _uiState.value.settings.shortBreakDurationMinutes * 60 * 1000L
    private fun getLongBreakDuration(): Long = _uiState.value.settings.longBreakDurationMinutes * 60 * 1000L

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }
}

data class TimerUiState(
    val settings: TimerSettings = TimerSettings(),
    val selectedSegment: PomodoroSegment = PomodoroSegment.FOCUS,
    val timerState: TimerState = TimerState.STOPPED,
    val remainingTimeMs: Long = 25L * 60 * 1000,
    val completedFocusSessions: Int = 0,
    val sessionStartTime: Date? = null
) {
    val sessionType: SessionType
        get() = when (selectedSegment) {
            PomodoroSegment.FOCUS -> SessionType.FOCUS
            PomodoroSegment.BREAK -> {
                if (completedFocusSessions > 0 && completedFocusSessions % 4 == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
            }
        }
}
