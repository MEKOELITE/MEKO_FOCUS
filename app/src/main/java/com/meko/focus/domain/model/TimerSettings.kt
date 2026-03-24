package com.meko.focus.domain.model

/**
 * 计时器设置数据模型
 *
 * 负责存储和验证番茄钟的各项配置参数。
 *
 * @property focusDurationMinutes 专注时长（分钟），有效范围 1-120
 * @property shortBreakDurationMinutes 短休息时长（分钟），有效范围 1-30
 * @property longBreakDurationMinutes 长休息时长（分钟），有效范围 1-60
 * @property autoSwitch 是否自动切换专注/休息状态
 * @property vibrationEnabled 是否启用振动提醒
 * @property soundEnabled 是否启用声音提醒
 * @property notificationsEnabled 是否启用通知
 * @property darkTheme 是否启用深色主题
 */
data class TimerSettings(
    val focusDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val autoSwitch: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val darkTheme: Boolean = false
) {
    /**
     * 清理后的专注时长，确保在有效范围内
     */
    val safeFocusDuration: Int
        get() = focusDurationMinutes.coerceIn(1, 120)

    /**
     * 清理后的短休息时长，确保在有效范围内
     */
    val safeShortBreakDuration: Int
        get() = shortBreakDurationMinutes.coerceIn(1, 30)

    /**
     * 清理后的长休息时长，确保在有效范围内
     */
    val safeLongBreakDuration: Int
        get() = longBreakDurationMinutes.coerceIn(1, 60)

    companion object {
        const val MIN_FOCUS_DURATION = 1
        const val MAX_FOCUS_DURATION = 120
        const val MIN_SHORT_BREAK = 1
        const val MAX_SHORT_BREAK = 30
        const val MIN_LONG_BREAK = 1
        const val MAX_LONG_BREAK = 60
        
        const val DEFAULT_FOCUS_DURATION = 25
        const val DEFAULT_SHORT_BREAK = 5
        const val DEFAULT_LONG_BREAK = 15
    }
}