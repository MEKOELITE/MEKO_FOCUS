package com.meko.focus.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class FocusSessionTest {

    @Test
    fun `focus session should have correct properties`() {
        val startTime = Date()
        val session = FocusSession(
            id = 1L,
            startTime = startTime,
            duration = 25 * 60 * 1000L,
            isCompleted = true,
            tag = "工作"
        )

        assertEquals(1L, session.id)
        assertEquals(startTime, session.startTime)
        assertEquals(25 * 60 * 1000L, session.duration)
        assertTrue(session.isCompleted)
        assertEquals("工作", session.tag)
    }

    @Test
    fun `completed session should be marked correctly`() {
        val session = FocusSession(
            startTime = Date(),
            duration = 1500000L,
            isCompleted = true,
            tag = "学习"
        )

        assertTrue(session.isCompleted)
        assertEquals(1500000L, session.duration)
    }

    @Test
    fun `incomplete session should have zero duration`() {
        val session = FocusSession(
            startTime = Date(),
            duration = 0L,
            isCompleted = false,
            tag = ""
        )

        assertFalse(session.isCompleted)
        assertEquals(0L, session.duration)
    }
}
