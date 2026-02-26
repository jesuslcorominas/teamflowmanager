package com.jesuslcorominas.teamflowmanager.data.remote.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InvitationCodeGeneratorTest {

    @Test
    fun `generate should return code with default length`() {
        // When
        val code = InvitationCodeGenerator.generate()

        // Then
        assertEquals(8, code.length)
    }

    @Test
    fun `generate should return code with custom length`() {
        // When
        val code = InvitationCodeGenerator.generate(6)

        // Then
        assertEquals(6, code.length)
    }

    @Test
    fun `generate should return code with only readable characters`() {
        // Given
        val readableChars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

        // When
        val code = InvitationCodeGenerator.generate()

        // Then
        code.forEach { char ->
            assertTrue("Code contains invalid character: $char", char in readableChars)
        }
    }

    @Test
    fun `generate should not contain ambiguous characters`() {
        // Given
        val ambiguousChars = "01OIl"

        // When - Generate multiple codes to increase probability
        val codes = (1..100).map { InvitationCodeGenerator.generate() }

        // Then
        codes.forEach { code ->
            code.forEach { char ->
                assertFalse(
                    "Code contains ambiguous character: $char in code: $code",
                    char in ambiguousChars
                )
            }
        }
    }

    @Test
    fun `generate should return different codes on multiple invocations`() {
        // When - Generate multiple codes
        val codes = (1..100).map { InvitationCodeGenerator.generate() }.toSet()

        // Then - Should have at least 90% unique codes (accounting for random collisions)
        assertTrue("Expected at least 90 unique codes, got ${codes.size}", codes.size >= 90)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generate should throw exception for length less than 6`() {
        // When
        InvitationCodeGenerator.generate(5)

        // Then - exception is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generate should throw exception for length greater than 10`() {
        // When
        InvitationCodeGenerator.generate(11)

        // Then - exception is thrown
    }
}
