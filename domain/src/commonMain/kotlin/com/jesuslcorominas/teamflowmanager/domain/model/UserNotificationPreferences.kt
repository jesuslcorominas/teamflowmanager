package com.jesuslcorominas.teamflowmanager.domain.model

data class TeamNotificationPreferences(
    val teamRemoteId: String,
    val matchEvents: Boolean = true,
    val goals: Boolean = true,
)

enum class GlobalNotificationState { ALL_ON, ALL_OFF, MIXED }

data class UserNotificationPreferences(
    val userId: String,
    val globalMatchEvents: Boolean = true,
    val globalGoals: Boolean = true,
    val teamPreferences: Map<String, TeamNotificationPreferences> = emptyMap(),
) {
    fun isEnabledFor(
        teamRemoteId: String,
        type: NotificationEventType,
    ): Boolean {
        val teamPref = teamPreferences[teamRemoteId]
        return when (type) {
            NotificationEventType.MATCH_EVENTS -> teamPref?.matchEvents ?: globalMatchEvents
            NotificationEventType.GOALS -> teamPref?.goals ?: globalGoals
        }
    }

    fun globalStateFor(type: NotificationEventType): GlobalNotificationState {
        val global = when (type) {
            NotificationEventType.MATCH_EVENTS -> globalMatchEvents
            NotificationEventType.GOALS -> globalGoals
        }
        if (teamPreferences.isEmpty()) {
            return if (global) GlobalNotificationState.ALL_ON else GlobalNotificationState.ALL_OFF
        }
        val hasOverride = teamPreferences.values.any { pref ->
            when (type) {
                NotificationEventType.MATCH_EVENTS -> pref.matchEvents != global
                NotificationEventType.GOALS -> pref.goals != global
            }
        }
        return if (hasOverride) GlobalNotificationState.MIXED else if (global) GlobalNotificationState.ALL_ON else GlobalNotificationState.ALL_OFF
    }
}
