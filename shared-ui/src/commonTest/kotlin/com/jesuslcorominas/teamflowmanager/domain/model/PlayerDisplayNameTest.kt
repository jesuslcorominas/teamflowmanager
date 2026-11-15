package com.jesuslcorominas.teamflowmanager.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerDisplayNameTest {
    private fun player(
        firstName: String,
        lastName: String,
    ) = Player(
        id = "1",
        firstName = firstName,
        lastName = lastName,
        number = 7,
        positions = emptyList(),
        teamId = "team",
        isCaptain = false,
    )

    @Test
    fun `joins first name and surname when both are present`() {
        assertEquals("Martin Guerra", player("Martin", "Guerra").displayName)
    }

    @Test
    fun `omits the surname when it is empty without leaving a trailing space`() {
        assertEquals("Javi", player("Javi", "").displayName)
    }

    @Test
    fun `omits the surname when it is only whitespace`() {
        assertEquals("Javi", player("Javi", "   ").displayName)
    }

    @Test
    fun `falls back to the surname when the first name is blank`() {
        assertEquals("Guerra", player("", "Guerra").displayName)
    }

    @Test
    fun `is empty when both parts are blank`() {
        assertEquals("", player("", "").displayName)
    }
}
