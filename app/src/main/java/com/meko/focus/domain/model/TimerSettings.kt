package com.meko.focus.domain.model

data class TimerSettings(
    val focusDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val autoSwitch: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val darkTheme: Boolean = false
)