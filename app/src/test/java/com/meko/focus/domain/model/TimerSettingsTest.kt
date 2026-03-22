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
        val settings = TimerSettings(focusDurationMinutes = 60)
        assertTrue(settings.focusDurationMinutes in 1..120)
    }

    @Test
    fun `boundary values should be accepted`() {
        val settings = TimerSettings(
            focusDurationMinutes = 1,
            shortBreakDurationMinutes = 1,
            longBreakDurationMinutes = 1
        )
        assertEquals(1, settings.focusDurationMinutes)
        assertEquals(1, settings.shortBreakDurationMinutes)
        assertEquals(1, settings.longBreakDurationMinutes)
    }

    @Test
    fun `max boundary values should be accepted`() {
        val settings = TimerSettings(
            focusDurationMinutes = 120,
            shortBreakDurationMinutes = 30,
            longBreakDurationMinutes = 60
        )
        assertEquals(120, settings.focusDurationMinutes)
        assertEquals(30, settings.shortBreakDurationMinutes)
        assertEquals(60, settings.longBreakDurationMinutes)
    }

    @Test
    fun `companion object constants should match validation range`() {
        assertEquals(1, TimerSettings.MIN_FOCUS_DURATION)
        assertEquals(120, TimerSettings.MAX_FOCUS_DURATION)
        assertEquals(1, TimerSettings.MIN_SHORT_BREAK)
        assertEquals(30, TimerSettings.MAX_SHORT_BREAK)
        assertEquals(1, TimerSettings.MIN_LONG_BREAK)
        assertEquals(60, TimerSettings.MAX_LONG_BREAK)
    }

    @Test
    fun `safeFocusDuration should clamp invalid values`() {
        val settings1 = TimerSettings(focusDurationMinutes = 0)
        assertEquals(1, settings1.safeFocusDuration)

        val settings2 = TimerSettings(focusDurationMinutes = 200)
        assertEquals(120, settings2.safeFocusDuration)

        val settings3 = TimerSettings(focusDurationMinutes = 50)
        assertEquals(50, settings3.safeFocusDuration)
    }

    @Test
    fun `safeShortBreakDuration should clamp invalid values`() {
        val settings1 = TimerSettings(shortBreakDurationMinutes = 0)
        assertEquals(1, settings1.safeShortBreakDuration)

        val settings2 = TimerSettings(shortBreakDurationMinutes = 100)
        assertEquals(30, settings2.safeShortBreakDuration)
    }

    @Test
    fun `safeLongBreakDuration should clamp invalid values`() {
        val settings1 = TimerSettings(longBreakDurationMinutes = 0)
        assertEquals(1, settings1.safeLongBreakDuration)

        val settings2 = TimerSettings(longBreakDurationMinutes = 100)
        assertEquals(60, settings2.safeLongBreakDuration)
    }

    @Test
    fun `default constants should have correct values`() {
        assertEquals(25, TimerSettings.DEFAULT_FOCUS_DURATION)
        assertEquals(5, TimerSettings.DEFAULT_SHORT_BREAK)
        assertEquals(15, TimerSettings.DEFAULT_LONG_BREAK)
    }
}
