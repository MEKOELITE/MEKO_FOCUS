package com.meko.focus.domain.repository

import org.junit.Assert.*
import org.junit.Test

class RepositoryExceptionTest {

    @Test
    fun `exception should have correct message`() {
        val exception = RepositoryException("Test error message")
        assertEquals("Test error message", exception.message)
    }

    @Test
    fun `exception should have cause when provided`() {
        val cause = RuntimeException("Original error")
        val exception = RepositoryException("Test error", cause)
        assertEquals("Test error", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `exception cause should be null when not provided`() {
        val exception = RepositoryException("Test error")
        assertNull(exception.cause)
    }

    @Test
    fun `exception should be catchable as Exception`() {
        val exception = RepositoryException("Test error")
        var caught = false
        try {
            throw exception
        } catch (e: Exception) {
            caught = true
        }
        assertTrue(caught)
    }
}
