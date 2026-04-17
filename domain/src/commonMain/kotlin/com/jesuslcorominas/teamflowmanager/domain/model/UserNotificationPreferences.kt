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
        if (teamPreferences.isEmpty()) {
            return when (type) {
                NotificationEventType.MATCH_EVENTS -> if (globalMatchEvents) GlobalNotificationState.ALL_ON else GlobalNotificationState.ALL_OFF
                NotificationEventType.GOALS -> if (globalGoals) GlobalNotificationState.ALL_ON else GlobalNotificationState.ALL_OFF
            }
        }
        val values =
            teamPreferences.values.map { pref ->
                when (type) {
                    NotificationEventType.MATCH_EVENTS -> pref.matchEvents
                    NotificationEventType.GOALS -> pref.goals
                }
            }
        return when {
            values.all { it } -> GlobalNotificationState.ALL_ON
            values.none { it } -> GlobalNotificationState.ALL_OFF
            else -> GlobalNotificationState.MIXED
        }
    }
}
