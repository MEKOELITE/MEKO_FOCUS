package com.meko.focus.domain.model

import org.junit.Assert.*
import org.junit.Test

class SessionTypeTest {

    @Test
    fun `SessionType should have all expected values`() {
        val values = SessionType.values()

        assertEquals(3, values.size)
        assertTrue(values.contains(SessionType.FOCUS))
        assertTrue(values.contains(SessionType.SHORT_BREAK))
        assertTrue(values.contains(SessionType.LONG_BREAK))
    }

    @Test
    fun `TimerState should have all expected values`() {
        val values = TimerState.values()

        assertEquals(3, values.size)
        assertTrue(values.contains(TimerState.STOPPED))
        assertTrue(values.contains(TimerState.RUNNING))
        assertTrue(values.contains(TimerState.PAUSED))
    }
}
