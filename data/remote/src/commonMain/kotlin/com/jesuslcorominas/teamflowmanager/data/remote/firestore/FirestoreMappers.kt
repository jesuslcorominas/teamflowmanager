package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.model.TeamNotificationPreferences
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences

fun ClubFields.toDomain(): Club =
    Club(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        homeGround = homeGround,
    )

fun PresidentNotificationFields.toDomain(): PresidentNotification =
    PresidentNotification(
        id = id,
        type =
            NotificationType.entries.firstOrNull { it.key == type }
                ?: NotificationType.USER_WAITING_FOR_ASSIGNMENT,
        title = title,
        body = body,
        userData = userData,
        createdAt = createdAt,
        read = read,
    )

fun NotificationPreferencesFields.toDomain(userId: String): UserNotificationPreferences =
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

fun Map<String, String>.toPendingCoachAssignment(): PendingCoachAssignment? {
    val teamId = this["teamId"] ?: return null
    val clubId = this["clubId"] ?: return null
    val email = this["email"] ?: return null
    return PendingCoachAssignment(teamId = teamId, clubId = clubId, email = email)
}
