package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.TeamNotificationPreferences
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences

data class NotificationPreferencesFirestoreModel(
    val matchEvents: Boolean = true,
    val goals: Boolean = true,
    val teams: Map<String, TeamPrefsModel> = emptyMap(),
) {
    data class TeamPrefsModel(
        val matchEvents: Boolean = true,
        val goals: Boolean = true,
    )
}

fun NotificationPreferencesFirestoreModel.toDomain(userId: String): UserNotificationPreferences =
    UserNotificationPreferences(
        userId = userId,
        globalMatchEvents = matchEvents,
        globalGoals = goals,
        teamPreferences =
            teams.mapValues { (teamId, prefs) ->
                TeamNotificationPreferences(
                    teamRemoteId = teamId,
                    matchEvents = prefs.matchEvents,
                    goals = prefs.goals,
                )
            },
    )
