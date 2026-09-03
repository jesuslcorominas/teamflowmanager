package com.jesuslcorominas.teamflowmanager.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserNotificationPreferencesTest {

    private val teamId = "team-fs-1"
    private val otherTeamId = "team-fs-2"

    // ── isEnabledFor ──────────────────────────────────────────────────────────

    @Test
    fun `isEnabledFor MATCH_EVENTS returns global when no team prefs exist`() {
        val prefs = UserNotificationPreferences(userId = "u1", globalMatchEvents = true, globalGoals = false)
        assertTrue(prefs.isEnabledFor(teamId, NotificationEventType.MATCH_EVENTS))
    }

    @Test
    fun `isEnabledFor GOALS returns global when no team prefs exist`() {
        val prefs = UserNotificationPreferences(userId = "u1", globalMatchEvents = true, globalGoals = false)
        assertFalse(prefs.isEnabledFor(teamId, NotificationEventType.GOALS))
    }

    @Test
    fun `isEnabledFor MATCH_EVENTS uses team override when present`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalMatchEvents = true,
            teamPreferences = mapOf(teamId to TeamNotificationPreferences(teamRemoteId = teamId, matchEvents = false)),
        )
        assertFalse(prefs.isEnabledFor(teamId, NotificationEventType.MATCH_EVENTS))
    }

    @Test
    fun `isEnabledFor GOALS uses team override when present`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalGoals = false,
            teamPreferences = mapOf(teamId to TeamNotificationPreferences(teamRemoteId = teamId, goals = true)),
        )
        assertTrue(prefs.isEnabledFor(teamId, NotificationEventType.GOALS))
    }

    @Test
    fun `isEnabledFor falls back to global when team pref is for a different team`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalMatchEvents = true,
            teamPreferences = mapOf(otherTeamId to TeamNotificationPreferences(teamRemoteId = otherTeamId, matchEvents = false)),
        )
        assertTrue(prefs.isEnabledFor(teamId, NotificationEventType.MATCH_EVENTS))
    }

    @Test
    fun `isEnabledFor returns false when global is off and no team override`() {
        val prefs = UserNotificationPreferences(userId = "u1", globalMatchEvents = false, globalGoals = false)
        assertFalse(prefs.isEnabledFor(teamId, NotificationEventType.MATCH_EVENTS))
        assertFalse(prefs.isEnabledFor(teamId, NotificationEventType.GOALS))
    }

    // ── globalStateFor ────────────────────────────────────────────────────────

    @Test
    fun `globalStateFor returns ALL_ON when global is true and no team prefs`() {
        val prefs = UserNotificationPreferences(userId = "u1", globalMatchEvents = true, globalGoals = true)
        assertEquals(GlobalNotificationState.ALL_ON, prefs.globalStateFor(NotificationEventType.MATCH_EVENTS))
        assertEquals(GlobalNotificationState.ALL_ON, prefs.globalStateFor(NotificationEventType.GOALS))
    }

    @Test
    fun `globalStateFor returns ALL_OFF when global is false and no team prefs`() {
        val prefs = UserNotificationPreferences(userId = "u1", globalMatchEvents = false, globalGoals = false)
        assertEquals(GlobalNotificationState.ALL_OFF, prefs.globalStateFor(NotificationEventType.MATCH_EVENTS))
        assertEquals(GlobalNotificationState.ALL_OFF, prefs.globalStateFor(NotificationEventType.GOALS))
    }

    @Test
    fun `globalStateFor returns MIXED when team pref overrides global for MATCH_EVENTS`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalMatchEvents = true,
            teamPreferences = mapOf(teamId to TeamNotificationPreferences(teamRemoteId = teamId, matchEvents = false)),
        )
        assertEquals(GlobalNotificationState.MIXED, prefs.globalStateFor(NotificationEventType.MATCH_EVENTS))
    }

    @Test
    fun `globalStateFor returns MIXED when team pref overrides global for GOALS`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalGoals = false,
            teamPreferences = mapOf(teamId to TeamNotificationPreferences(teamRemoteId = teamId, goals = true)),
        )
        assertEquals(GlobalNotificationState.MIXED, prefs.globalStateFor(NotificationEventType.GOALS))
    }

    @Test
    fun `globalStateFor returns ALL_ON when team pref agrees with global true`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalMatchEvents = true,
            teamPreferences = mapOf(teamId to TeamNotificationPreferences(teamRemoteId = teamId, matchEvents = true)),
        )
        assertEquals(GlobalNotificationState.ALL_ON, prefs.globalStateFor(NotificationEventType.MATCH_EVENTS))
    }

    @Test
    fun `globalStateFor returns ALL_OFF when team pref agrees with global false`() {
        val prefs = UserNotificationPreferences(
            userId = "u1",
            globalGoals = false,
            teamPreferences = mapOf(teamId to TeamNotificationPreferences(teamRemoteId = teamId, goals = false)),
        )
        assertEquals(GlobalNotificationState.ALL_OFF, prefs.globalStateFor(NotificationEventType.GOALS))
    }
}