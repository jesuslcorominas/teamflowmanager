package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferencesFirestoreMappersTest {

    private fun teamPrefsFields(
        matchEvents: Boolean = true,
        goals: Boolean = true,
    ): TeamPrefsFields = object : TeamPrefsFields {
        override val matchEvents = matchEvents
        override val goals = goals
    }

    private fun prefsFields(
        matchEvents: Boolean = true,
        goals: Boolean = true,
        teams: Map<String, TeamPrefsFields> = emptyMap(),
    ): NotificationPreferencesFields = object : NotificationPreferencesFields {
        override val matchEvents = matchEvents
        override val goals = goals
        override val teams = teams
    }

    @Test
    fun `toDomain maps globalMatchEvents correctly`() {
        val result = prefsFields(matchEvents = false).toDomain("user-1")
        assertFalse(result.globalMatchEvents)
    }

    @Test
    fun `toDomain maps globalGoals correctly`() {
        val result = prefsFields(goals = false).toDomain("user-1")
        assertFalse(result.globalGoals)
    }

    @Test
    fun `toDomain maps userId correctly`() {
        val result = prefsFields().toDomain("user-abc")
        assertEquals("user-abc", result.userId)
    }

    @Test
    fun `toDomain maps empty teams correctly`() {
        val result = prefsFields(teams = emptyMap()).toDomain("user-1")
        assertTrue(result.teamPreferences.isEmpty())
    }

    @Test
    fun `toDomain maps single team entry correctly`() {
        val result = prefsFields(
            teams = mapOf("team-1" to teamPrefsFields(matchEvents = false, goals = true)),
        ).toDomain("user-1")

        assertEquals(1, result.teamPreferences.size)
        assertEquals("team-1", result.teamPreferences["team-1"]?.teamRemoteId)
        assertFalse(result.teamPreferences["team-1"]!!.matchEvents)
        assertTrue(result.teamPreferences["team-1"]!!.goals)
    }

    @Test
    fun `toDomain maps multiple team entries correctly`() {
        val result = prefsFields(
            teams = mapOf(
                "team-1" to teamPrefsFields(matchEvents = true, goals = false),
                "team-2" to teamPrefsFields(matchEvents = false, goals = true),
            ),
        ).toDomain("user-1")

        assertEquals(2, result.teamPreferences.size)
        assertTrue(result.teamPreferences["team-1"]!!.matchEvents)
        assertFalse(result.teamPreferences["team-1"]!!.goals)
        assertFalse(result.teamPreferences["team-2"]!!.matchEvents)
        assertTrue(result.teamPreferences["team-2"]!!.goals)
    }

    @Test
    fun `toDomain nested team matchEvents maps correctly`() {
        val result = prefsFields(
            teams = mapOf("team-x" to teamPrefsFields(matchEvents = false)),
        ).toDomain("user-1")

        assertFalse(result.teamPreferences["team-x"]!!.matchEvents)
    }

    @Test
    fun `toDomain nested team goals maps correctly`() {
        val result = prefsFields(
            teams = mapOf("team-y" to teamPrefsFields(goals = false)),
        ).toDomain("user-1")

        assertFalse(result.teamPreferences["team-y"]!!.goals)
    }

    @Test
    fun `toDomain global true values map correctly`() {
        val result = prefsFields(matchEvents = true, goals = true).toDomain("user-1")
        assertTrue(result.globalMatchEvents)
        assertTrue(result.globalGoals)
    }
}
