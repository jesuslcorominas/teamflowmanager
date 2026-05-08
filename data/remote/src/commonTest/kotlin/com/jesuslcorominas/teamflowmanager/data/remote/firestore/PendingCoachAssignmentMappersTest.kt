package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PendingCoachAssignmentMappersTest {

    @Test
    fun `map with all three fields returns valid PendingCoachAssignment`() {
        val result = mapOf(
            "teamId" to "team-1",
            "clubId" to "club-1",
            "email" to "coach@example.com",
        ).toPendingCoachAssignment()

        assertNotNull(result)
        assertEquals("team-1", result.teamId)
        assertEquals("club-1", result.clubId)
        assertEquals("coach@example.com", result.email)
    }

    @Test
    fun `map missing teamId returns null`() {
        val result = mapOf(
            "clubId" to "club-1",
            "email" to "coach@example.com",
        ).toPendingCoachAssignment()

        assertNull(result)
    }

    @Test
    fun `map missing clubId returns null`() {
        val result = mapOf(
            "teamId" to "team-1",
            "email" to "coach@example.com",
        ).toPendingCoachAssignment()

        assertNull(result)
    }

    @Test
    fun `map missing email returns null`() {
        val result = mapOf(
            "teamId" to "team-1",
            "clubId" to "club-1",
        ).toPendingCoachAssignment()

        assertNull(result)
    }

    @Test
    fun `empty map returns null`() {
        val result = emptyMap<String, String>().toPendingCoachAssignment()
        assertNull(result)
    }
}
