package com.meko.focus.data.repository

import com.meko.focus.data.local.mapper.toDomain
import com.meko.focus.data.local.mapper.toEntity
import com.meko.focus.domain.model.FocusSession
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class FocusSessionMapperTest {

    @Test
    fun `toEntity should convert domain to entity correctly`() {
        val startTime = Date()
        val domain = FocusSession(
            id = 1L,
            startTime = startTime,
            duration = 1500000L,
            isCompleted = true,
            tag = "测试"
        )

        val entity = domain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals(startTime, entity.startTime)
        assertEquals(1500000L, entity.duration)
        assertTrue(entity.isCompleted)
        assertEquals("测试", entity.tag)
    }

    @Test
    fun `toDomain should convert entity to domain correctly`() {
        val startTime = Date()
        val entity = com.meko.focus.data.local.entity.FocusSessionEntity(
            id = 2L,
            startTime = startTime,
            duration = 3000000L,
            isCompleted = false,
            tag = "学习"
        )

        val domain = entity.toDomain()

        assertEquals(2L, domain.id)
        assertEquals(startTime, domain.startTime)
        assertEquals(3000000L, domain.duration)
        assertFalse(domain.isCompleted)
        assertEquals("学习", domain.tag)
    }

    @Test
    fun `round trip conversion should preserve data`() {
        val original = FocusSession(
            id = 5L,
            startTime = Date(),
            duration = 600000L,
            isCompleted = true,
            tag = "工作"
        )

        val entity = original.toEntity()
        val result = entity.toDomain()

        assertEquals(original.id, result.id)
        assertEquals(original.duration, result.duration)
        assertEquals(original.isCompleted, result.isCompleted)
        assertEquals(original.tag, result.tag)
    }

    @Test
    fun `conversion should handle null tag`() {
        val domain = FocusSession(
            id = 1L,
            startTime = Date(),
            duration = 1500000L,
            isCompleted = true,
            tag = null
        )

        val entity = domain.toEntity()
        assertNull(entity.tag)

        val result = entity.toDomain()
        assertNull(result.tag)
    }
}
