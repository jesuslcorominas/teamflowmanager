package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPreferencesFirestoreModelTest {

    @Test
    fun `toDomain with empty model returns default preferences`() {
        val model = NotificationPreferencesFirestoreModel()
        val userId = "user-123"

        val result = model.toDomain(userId)

        assertEquals("user-123", result.userId)
        assertTrue(result.globalMatchEvents)
        assertTrue(result.globalGoals)
        assertTrue(result.teamPreferences.isEmpty())
    }

    @Test
    fun `toDomain with global preferences returns correct values`() {
        val model = NotificationPreferencesFirestoreModel(
            matchEvents = false,
            goals = true,
            teams = emptyMap(),
        )
        val userId = "user-456"

        val result = model.toDomain(userId)

        assertEquals("user-456", result.userId)
        assertEquals(false, result.globalMatchEvents)
        assertEquals(true, result.globalGoals)
    }

    @Test
    fun `toDomain with team preferences maps correctly`() {
        val model = NotificationPreferencesFirestoreModel(
            matchEvents = true,
            goals = true,
            teams = mapOf(
                "team-1" to NotificationPreferencesFirestoreModel.TeamPrefsModel(
                    matchEvents = false,
                    goals = true,
                ),
                "team-2" to NotificationPreferencesFirestoreModel.TeamPrefsModel(
                    matchEvents = true,
                    goals = false,
                ),
            ),
        )
        val userId = "user-789"

        val result = model.toDomain(userId)

        assertEquals("user-789", result.userId)
        assertEquals(2, result.teamPreferences.size)
        assertEquals(false, result.teamPreferences["team-1"]?.matchEvents)
        assertEquals(true, result.teamPreferences["team-1"]?.goals)
        assertEquals(true, result.teamPreferences["team-2"]?.matchEvents)
        assertEquals(false, result.teamPreferences["team-2"]?.goals)
    }

    @Test
    fun `toDomain with mixed preferences returns complete mapping`() {
        val model = NotificationPreferencesFirestoreModel(
            matchEvents = false,
            goals = false,
            teams = mapOf(
                "team-a" to NotificationPreferencesFirestoreModel.TeamPrefsModel(
                    matchEvents = true,
                    goals = true,
                ),
            ),
        )
        val userId = "user-xyz"

        val result = model.toDomain(userId)

        assertEquals("user-xyz", result.userId)
        assertEquals(false, result.globalMatchEvents)
        assertEquals(false, result.globalGoals)
        assertEquals(true, result.teamPreferences["team-a"]?.matchEvents)
        assertEquals(true, result.teamPreferences["team-a"]?.goals)
    }
}
