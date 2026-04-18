package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import kotlinx.coroutines.flow.first

internal class NotifyPresidentMatchEventUseCaseImpl(
    private val clubRepository: ClubRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
    private val fcmNotificationRepository: FcmNotificationRepository,
    private val presidentNotificationRepository: PresidentNotificationRepository,
) : NotifyPresidentMatchEventUseCase {
    override suspend fun invoke(
        event: MatchEventNotification,
        matchId: String,
        teamRemoteId: String,
        clubRemoteId: String,
    ) {
        try {
            val club = clubRepository.getClubById(clubRemoteId) ?: return
            val presidentUserId = club.ownerId

            val eventType =
                when (event) {
                    is MatchEventNotification.Start -> NotificationEventType.MATCH_EVENTS
                    is MatchEventNotification.End -> NotificationEventType.MATCH_EVENTS
                    is MatchEventNotification.Goal -> NotificationEventType.GOALS
                }

            val prefs =
                notificationPreferencesRepository
                    .getPreferences(presidentUserId, clubRemoteId)
                    .first()
            if (!prefs.isEnabledFor(teamRemoteId, eventType)) return

            val payload = buildPayload(event)

            presidentNotificationRepository.createNotification(
                clubId = clubRemoteId,
                notification = buildFirestoreNotification(event, matchId),
            )

            fcmNotificationRepository.sendNotificationToUser(presidentUserId, payload)
        } catch (e: Exception) {
            // Notification failure must not affect match flow
        }
    }

    private fun buildFirestoreNotification(
        event: MatchEventNotification,
        matchId: String,
    ): PresidentNotification {
        val (type, title, body) =
            when (event) {
                is MatchEventNotification.Start ->
                    Triple(
                        NotificationType.MATCH_START,
                        "Comienza el partido de ${event.teamName}",
                        "${event.teamName} vs ${event.opponent}",
                    )
                is MatchEventNotification.End ->
                    Triple(
                        NotificationType.MATCH_END,
                        "Fin del partido — ${event.teamName} ${event.teamGoals}-${event.opponentGoals} ${event.opponent}",
                        "Resultado final: ${event.teamGoals}-${event.opponentGoals}",
                    )
                is MatchEventNotification.Goal -> {
                    val scoringTeam = if (event.isOpponentGoal) event.opponentName else event.teamName
                    val minute = event.minuteOfPlay?.let { " (min. $it')" } ?: ""
                    Triple(
                        NotificationType.GOAL,
                        "Gol de $scoringTeam$minute",
                        "${event.teamName} ${event.teamGoals}-${event.opponentGoals} ${event.opponentName}",
                    )
                }
            }
        return PresidentNotification(
            id = "match_$matchId",
            type = type,
            title = title,
            body = body,
            userData = emptyMap(),
            createdAt = System.currentTimeMillis(),
            read = false,
        )
    }

    private fun buildPayload(event: MatchEventNotification): NotificationPayload =
        when (event) {
            is MatchEventNotification.Start ->
                NotificationPayload.Typed.MatchStart(event.teamName, event.opponent)
            is MatchEventNotification.End ->
                NotificationPayload.Typed.MatchEnd(event.teamName, event.opponent, event.teamGoals, event.opponentGoals)
            is MatchEventNotification.Goal ->
                NotificationPayload.Typed.GoalScored(
                    teamName = event.teamName,
                    opponentName = event.opponentName,
                    teamGoals = event.teamGoals,
                    opponentGoals = event.opponentGoals,
                    minuteOfPlay = event.minuteOfPlay,
                    isOpponentGoal = event.isOpponentGoal,
                )
        }
}
