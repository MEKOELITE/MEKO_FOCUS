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
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * 计时器 ViewModel
 *
 * 管理计时器 UI 状态，处理用户交互，协调服务和数据层。
 *
 * @property context 应用上下文
 * @property focusSessionRepository 专注会话仓库
 * @property settingsRepository 设置仓库
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusSessionRepository: FocusSessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /** UI 状态流 */
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    /** 标记是否已加载设置（用于首次初始化） */
    private var settingsLoaded = false

    /** 绑定的计时器服务 */
    private var timerService: TimerForegroundService? = null
    private var serviceBound = false

    /**
     * 服务连接回调
     *
     * 当成功绑定到 TimerForegroundService 时，建立状态同步。
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.TimerBinder
            timerService = binder.getService()
            serviceBound = true

            // 监听服务时间变化，仅在计时器运行时同步
            viewModelScope.launch {
                timerService?.remainingTimeMs?.collect { time ->
                    // 只有在计时器运行时才同步服务时间到 UI
                    // 避免服务初始值 0 覆盖正确的 UI 状态
                    if (_uiState.value.timerState == TimerState.RUNNING && time > 0) {
                        _uiState.value = _uiState.value.copy(remainingTimeMs = time)
                    }
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
        // 监听设置变化
        viewModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                val currentState = _uiState.value

                if (!settingsLoaded || currentState.settings != settings) {
                    // 首次加载或设置变更时，计算新的剩余时间
                    val newRemainingTime = when (currentState.timerState) {
                        TimerState.RUNNING -> {
                            // 倒计时进行中：不改动剩余时间，避免打断当前会话
                            currentState.remainingTimeMs
                        }
                        TimerState.STOPPED, TimerState.PAUSED -> {
                            // 未在倒计时或已暂停：应用新的预设时长（修改设置后应立刻反映在主界面）
                            when (currentState.selectedSegment) {
                                PomodoroSegment.FOCUS -> settings.focusDurationMinutes * 60 * 1000L
                                PomodoroSegment.BREAK -> getBreakDuration(
                                    settings,
                                    currentState.completedFocusSessions
                                )
                            }
                        }
                    }

                    _uiState.value = currentState.copy(
                        settings = settings,
                        remainingTimeMs = newRemainingTime
                    )
                    settingsLoaded = true
                }
            }
        }

        bindService()
    }

    /**
     * 计算剩余时间
     *
     * @param segment 当前分段
     * @param settings 当前设置
     * @param timerState 当前计时器状态
     * @param currentRemainingTime 当前剩余时间
     * @return 新的剩余时间
     */
    private fun calculateRemainingTime(
        segment: PomodoroSegment,
        settings: TimerSettings,
        timerState: TimerState,
        currentRemainingTime: Long
    ): Long {
        return when (timerState) {
            TimerState.STOPPED -> {
                // 停止状态：使用设置中的默认时间
                when (segment) {
                    PomodoroSegment.FOCUS -> settings.focusDurationMinutes * 60 * 1000L
                    PomodoroSegment.BREAK -> settings.shortBreakDurationMinutes * 60 * 1000L
                }
            }
            TimerState.RUNNING, TimerState.PAUSED -> {
                // 运行或暂停状态：保持当前时间
                currentRemainingTime
            }
        }
    }

    private fun bindService() {
        val intent = Intent(context, TimerForegroundService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /** 本地倒计时任务 */
    private var timerJob: Job? = null

    /**
     * 切换计时器状态
     *
     * 根据当前状态执行启动/暂停/继续操作。
     */
    fun toggleTimer() {
        when (_uiState.value.timerState) {
            TimerState.STOPPED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
            TimerState.PAUSED -> resumeTimer()
        }
    }

    /**
     * 选择会话分段
     *
     * @param segment 选中的分段（专注/休息）
     */
    fun selectSegment(segment: PomodoroSegment) {
        stopTimer()
        val newTime = when (segment) {
            PomodoroSegment.FOCUS -> _uiState.value.settings.focusDurationMinutes * 60 * 1000L
            PomodoroSegment.BREAK -> getBreakDuration(_uiState.value.settings, _uiState.value.completedFocusSessions)
        }
        _uiState.value = _uiState.value.copy(
            selectedSegment = segment,
            sessionStartTime = null,
            remainingTimeMs = newTime
        )
    }

    /**
     * 重置计时器
     */
    fun resetTimer() {
        stopTimer()
        val newTime = when (_uiState.value.selectedSegment) {
            PomodoroSegment.FOCUS -> _uiState.value.settings.focusDurationMinutes * 60 * 1000L
            PomodoroSegment.BREAK -> getBreakDuration(_uiState.value.settings, _uiState.value.completedFocusSessions)
        }
        _uiState.value = _uiState.value.copy(
            remainingTimeMs = newTime,
            timerState = TimerState.STOPPED,
            sessionStartTime = null
        )
    }

    /**
     * 启动计时器
     *
     * 包含防御性检查，确保剩余时间有效。
     */
    private fun startTimer() {
        val currentState = _uiState.value
        
        // 防御性检查：如果剩余时间为0或负数，使用默认值
        val validRemainingTime = if (currentState.remainingTimeMs <= 0) {
            when (currentState.selectedSegment) {
                PomodoroSegment.FOCUS -> currentState.settings.focusDurationMinutes * 60 * 1000L
                PomodoroSegment.BREAK -> getBreakDuration(currentState.settings, currentState.completedFocusSessions)
            }
        } else {
            currentState.remainingTimeMs
        }

        _uiState.value = currentState.copy(
            timerState = TimerState.RUNNING,
            sessionStartTime = Date(),
            remainingTimeMs = validRemainingTime
        )

        TimerForegroundService.startTimer(
            context,
            validRemainingTime,
            _uiState.value.selectedSegment.name
        )

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
                val currentTime = _uiState.value.remainingTimeMs
                if (currentTime > 0) {
                    _uiState.value = _uiState.value.copy(remainingTimeMs = currentTime - 1000)
                }
            }
            // 只有在计时器仍在运行状态时才触发完成（避免竞态条件）
            if (_uiState.value.timerState == TimerState.RUNNING && _uiState.value.remainingTimeMs <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun stopLocalCountdown() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * 计时器完成回调
     *
     * 触发完成音效、振动，并处理自动切换逻辑。
     */
    private fun onTimerFinished() {
        val currentState = _uiState.value
        val settings = currentState.settings

        if (settings.vibrationEnabled) {
            VibrationHelper.vibrateTimerComplete(context)
        }

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
                            getLongBreakDuration(settings)
                        } else {
                            getShortBreakDuration(settings)
                        },
                        timerState = TimerState.STOPPED,
                        sessionStartTime = null
                    )
                }
                PomodoroSegment.BREAK -> {
                    _uiState.value = currentState.copy(
                        selectedSegment = PomodoroSegment.FOCUS,
                        remainingTimeMs = getFocusDuration(settings),
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

        TimerForegroundService.stopTimer(context)
    }

    /**
     * 保存专注会话记录
     */
    private fun saveFocusSession(state: TimerUiState) {
        val startTime = state.sessionStartTime ?: return

        viewModelScope.launch {
            try {
                val session = FocusSession(
                    startTime = startTime,
                    duration = getFocusDuration(state.settings) - state.remainingTimeMs,
                    isCompleted = true,
                    tag = "专注"
                )
                focusSessionRepository.insertSession(session)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getFocusDuration(settings: TimerSettings): Long = settings.focusDurationMinutes * 60 * 1000L
    private fun getShortBreakDuration(settings: TimerSettings): Long = settings.shortBreakDurationMinutes * 60 * 1000L
    private fun getLongBreakDuration(settings: TimerSettings): Long = settings.longBreakDurationMinutes * 60 * 1000L

    /**
     * 根据当前完成的专注轮数获取休息时长
     *
     * @param settings 当前设置
     * @param completedFocusSessions 已完成的专注轮数
     * @return 休息时长（毫秒）
     */
    private fun getBreakDuration(settings: TimerSettings, completedFocusSessions: Int): Long {
        val isLongBreak = completedFocusSessions > 0 && completedFocusSessions % 4 == 0
        return if (isLongBreak) {
            getLongBreakDuration(settings)
        } else {
            getShortBreakDuration(settings)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }
}

/**
 * 计时器 UI 状态数据类
 *
 * 包含计时器界面需要展示的所有状态信息。
 *
 * @property settings 当前计时器设置
 * @property selectedSegment 当前选中的分段
 * @property timerState 当前计时器状态
 * @property remainingTimeMs 剩余时间（毫秒）
 * @property completedFocusSessions 已完成的专注会话数
 * @property sessionStartTime 当前会话开始时间
 */
data class TimerUiState(
    val settings: TimerSettings = TimerSettings(),
    val selectedSegment: PomodoroSegment = PomodoroSegment.FOCUS,
    val timerState: TimerState = TimerState.STOPPED,
    val remainingTimeMs: Long = 25L * 60 * 1000,
    val completedFocusSessions: Int = 0,
    val sessionStartTime: Date? = null
) {
    /** 根据选中的分段和完成数判断会话类型 */
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
