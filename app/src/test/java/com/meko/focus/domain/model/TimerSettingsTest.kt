package com.meko.focus.domain.model

import org.junit.Assert.*
import org.junit.Test

class TimerSettingsTest {

    @Test
    fun `default settings should have correct values`() {
        val settings = TimerSettings()

        assertEquals(25, settings.focusDurationMinutes)
        assertEquals(5, settings.shortBreakDurationMinutes)
        assertEquals(15, settings.longBreakDurationMinutes)
        assertTrue(settings.autoSwitch)
        assertTrue(settings.vibrationEnabled)
        assertTrue(settings.soundEnabled)
        assertTrue(settings.notificationsEnabled)
        assertFalse(settings.darkTheme)
    }

    @Test
    fun `custom settings should preserve values`() {
        val settings = TimerSettings(
            focusDurationMinutes = 30,
            shortBreakDurationMinutes = 10,
            longBreakDurationMinutes = 20,
            autoSwitch = false,
            vibrationEnabled = false,
            soundEnabled = false,
            notificationsEnabled = false,
            darkTheme = true
        )

        assertEquals(30, settings.focusDurationMinutes)
        assertEquals(10, settings.shortBreakDurationMinutes)
        assertEquals(20, settings.longBreakDurationMinutes)
        assertFalse(settings.autoSwitch)
        assertFalse(settings.vibrationEnabled)
        assertFalse(settings.soundEnabled)
        assertFalse(settings.notificationsEnabled)
        assertTrue(settings.darkTheme)
    }

    @Test
    fun `focus duration should be within valid range`() {
        // Valid range: 1-120 minutes
        val settings = TimerSettings(focusDurationMinutes = 60)
        assertTrue(settings.focusDurationMinutes in 1..120)
    }
}
