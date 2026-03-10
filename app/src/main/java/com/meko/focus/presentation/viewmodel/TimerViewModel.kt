package com.meko.focus.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meko.focus.domain.model.SessionType
import com.meko.focus.domain.model.TimerState
import com.meko.focus.presentation.component.PomodoroSegment
import com.meko.focus.util.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // 计时器作业
    private var timerJob: Job? = null

    // 操作
    fun toggleTimer() {
        when (_uiState.value.timerState) {
            TimerState.STOPPED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
            TimerState.PAUSED -> resumeTimer()
        }
    }

    fun selectSegment(segment: PomodoroSegment) {
        _uiState.value = _uiState.value.copy(selectedSegment = segment)
        // 重置时间为该分段的默认时间
        _uiState.value = _uiState.value.copy(
            remainingTimeMs = getDefaultTimeForSegment(segment)
        )
    }

    fun resetTimer() {
        stopTimer()
        _uiState.value = _uiState.value.copy(
            remainingTimeMs = getDefaultTimeForSegment(_uiState.value.selectedSegment),
            timerState = TimerState.STOPPED
        )
    }

    private fun startTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.RUNNING)
        startCountdown()
    }

    private fun pauseTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
        stopCountdown()
    }

    private fun resumeTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.RUNNING)
        startCountdown()
    }

    private fun stopTimer() {
        _uiState.value = _uiState.value.copy(timerState = TimerState.STOPPED)
        stopCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerState == TimerState.RUNNING && _uiState.value.remainingTimeMs > 0) {
                delay(1000) // 1秒
                _uiState.value = _uiState.value.copy(remainingTimeMs = _uiState.value.remainingTimeMs - 1000)
            }
            // 计时结束
            if (_uiState.value.remainingTimeMs <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun stopCountdown() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun onTimerFinished() {
        val currentState = _uiState.value

        when (currentState.selectedSegment) {
            PomodoroSegment.FOCUS -> {
                // 完成一个专注会话
                val newCompletedSessions = currentState.completedFocusSessions + 1
                val isLongBreak = newCompletedSessions % 4 == 0

                _uiState.value = currentState.copy(
                    selectedSegment = PomodoroSegment.BREAK,
                    completedFocusSessions = newCompletedSessions,
                    remainingTimeMs = if (isLongBreak) {
                        getLongBreakDuration()
                    } else {
                        getShortBreakDuration()
                    },
                    timerState = TimerState.STOPPED
                )
            }
            PomodoroSegment.BREAK -> {
                // 休息结束，开始下一个专注
                _uiState.value = currentState.copy(
                    selectedSegment = PomodoroSegment.FOCUS,
                    remainingTimeMs = getFocusDuration(),
                    timerState = TimerState.STOPPED
                )
            }
        }

        // 触发振动提醒
        VibrationHelper.vibrateTimerComplete(context)

        // TODO: 触发声音提醒
        // TODO: 显示励志语录
    }

    private fun getDefaultTimeForSegment(segment: PomodoroSegment): Long {
        return when (segment) {
            PomodoroSegment.FOCUS -> getFocusDuration()
            PomodoroSegment.BREAK -> getShortBreakDuration() // 默认短休
        }
    }

    private fun getFocusDuration(): Long = 25L * 60 * 1000 // 25分钟
    private fun getShortBreakDuration(): Long = 5L * 60 * 1000 // 5分钟
    private fun getLongBreakDuration(): Long = 15L * 60 * 1000 // 15分钟

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class TimerUiState(
    val selectedSegment: PomodoroSegment = PomodoroSegment.FOCUS,
    val timerState: TimerState = TimerState.STOPPED,
    val remainingTimeMs: Long = 25L * 60 * 1000, // 默认25分钟
    val completedFocusSessions: Int = 0
) {
    val sessionType: SessionType
        get() = when (selectedSegment) {
            PomodoroSegment.FOCUS -> SessionType.FOCUS
            PomodoroSegment.BREAK -> {
                // 根据completedFocusSessions判断是短休还是长休
                if (completedFocusSessions > 0 && completedFocusSessions % 4 == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
            }
        }
}