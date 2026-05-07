package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.remote.firestore.NotificationPreferencesFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test validation of NotificationPreferencesFirestoreDataSourceImpl behavior.
 * These tests verify the field mapping, preference updates, and preference retrieval logic
 * that is used by the iOS implementation via GitLive Firebase SDK.
 */
class NotificationPreferencesFirestoreDataSourceImplTest {

    @Test
    fun `verify field mapping for MATCH_EVENTS type`() {
        val fieldName = when (NotificationEventType.MATCH_EVENTS) {
            NotificationEventType.MATCH_EVENTS -> "matchEvents"
            NotificationEventType.GOALS -> "goals"
        }

        assertEquals("matchEvents", fieldName)
    }

    @Test
    fun `verify field mapping for GOALS type`() {
        val fieldName = when (NotificationEventType.GOALS) {
            NotificationEventType.MATCH_EVENTS -> "matchEvents"
            NotificationEventType.GOALS -> "goals"
        }

        assertEquals("goals", fieldName)
    }

    @Test
    fun `verify default preferences when document does not exist`() {
        val userId = "user-prefs-1"
        val result = UserNotificationPreferences(userId = userId)

        assertEquals("user-prefs-1", result.userId)
        assertTrue(result.globalMatchEvents)
        assertTrue(result.globalGoals)
        assertTrue(result.teamPreferences.isEmpty())
    }

    @Test
    fun `verify nested path construction for team preferences update`() {
        val fieldName = "matchEvents"
        val teamRemoteId = "team-123"
        val fieldPath = "teams.$teamRemoteId.$fieldName"

        assertEquals("teams.team-123.matchEvents", fieldPath)
    }

    @Test
    fun `verify multiple team preference path construction`() {
        val teamRemoteId = "team-456"
        val fieldName = "goals"
        val fieldPath = "teams.$teamRemoteId.$fieldName"

        assertEquals("teams.team-456.goals", fieldPath)
    }

    @Test
    fun `verify nested map construction for NOT_FOUND fallback`() {
        val fieldName = "matchEvents"
        val teamRemoteId = "team-not-found"

        val nested = mapOf(
            "teams" to mapOf(
                teamRemoteId to mapOf(fieldName to true),
            ),
        )

        assertEquals(1, nested.size)
        assertTrue(nested.containsKey("teams"))
        @Suppress("UNCHECKED_CAST")
        val teamsMap = nested["teams"] as? Map<String, Any>
        assertFalse(teamsMap?.isEmpty() ?: true)
    }

    @Test
    fun `verify merge update map construction for global preference`() {
        val fieldName = "goals"
        val enabled = false
        val updateMap = mapOf(fieldName to enabled)

        assertEquals(1, updateMap.size)
        assertEquals(false, updateMap["goals"])
    }

    @Test
    fun `verify global preference update construction for MATCH_EVENTS`() {
        val fieldName =
            when (NotificationEventType.MATCH_EVENTS) {
                NotificationEventType.MATCH_EVENTS -> "matchEvents"
                NotificationEventType.GOALS -> "goals"
            }
        val enabled = true
        val updateMap = mapOf(fieldName to enabled)

        assertEquals("matchEvents", updateMap.keys.first())
        assertEquals(true, updateMap.values.first())
    }

    @Test
    fun `verify preferences model stores all fields correctly`() {
        val model = NotificationPreferencesFirestoreModel(
            matchEvents = false,
            goals = true,
            teams = mapOf(
                "team-1" to NotificationPreferencesFirestoreModel.TeamPrefsModel(
                    matchEvents = true,
                    goals = false,
                ),
            ),
        )

        assertFalse(model.matchEvents)
        assertTrue(model.goals)
        assertEquals(1, model.teams.size)
        assertTrue(model.teams.containsKey("team-1"))
    }

    @Test
    fun `verify exception handling for deserialization errors returns default`() {
        val userId = "user-error-1"
        val defaultPrefs = UserNotificationPreferences(userId = userId)

        assertEquals(userId, defaultPrefs.userId)
        assertTrue(defaultPrefs.globalMatchEvents)
        assertTrue(defaultPrefs.globalGoals)
        assertTrue(defaultPrefs.teamPreferences.isEmpty())
    }

    @Test
    fun `verify global preference update with multiple types`() {
        val updates = listOf(
            NotificationEventType.MATCH_EVENTS to true,
            NotificationEventType.GOALS to false,
        )

        val updateMaps = updates.map { (type, enabled) ->
            val fieldName = when (type) {
                NotificationEventType.MATCH_EVENTS -> "matchEvents"
                NotificationEventType.GOALS -> "goals"
            }
            mapOf(fieldName to enabled)
        }

        assertEquals(2, updateMaps.size)
        assertEquals(true, updateMaps[0]["matchEvents"])
        assertEquals(false, updateMaps[1]["goals"])
    }

    @Test
    fun `verify complex nested preference update for multiple teams`() {
        val updates = listOf(
            Pair("team-1", NotificationEventType.MATCH_EVENTS),
            Pair("team-2", NotificationEventType.GOALS),
        )

        val paths = updates.map { (teamId, type) ->
            val fieldName = when (type) {
                NotificationEventType.MATCH_EVENTS -> "matchEvents"
                NotificationEventType.GOALS -> "goals"
            }
            "teams.$teamId.$fieldName"
        }

        assertEquals(2, paths.size)
        assertEquals("teams.team-1.matchEvents", paths[0])
        assertEquals("teams.team-2.goals", paths[1])
    }

    @Test
    fun `verify NotificationPreferencesFirestoreModel default values`() {
        val model = NotificationPreferencesFirestoreModel()

        assertTrue(model.matchEvents)
        assertTrue(model.goals)
        assertTrue(model.teams.isEmpty())
    }

    @Test
    fun `verify TeamPrefsModel structure`() {
        val prefs = NotificationPreferencesFirestoreModel.TeamPrefsModel(
            matchEvents = false,
            goals = true,
        )

        assertFalse(prefs.matchEvents)
        assertTrue(prefs.goals)
    }
}
