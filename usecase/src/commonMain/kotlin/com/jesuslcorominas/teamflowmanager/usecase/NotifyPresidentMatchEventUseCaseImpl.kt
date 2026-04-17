package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

internal class NotifyPresidentMatchEventUseCaseImpl(
    private val clubMemberRepository: ClubMemberRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
    private val fcmNotificationRepository: FcmNotificationRepository,
) : NotifyPresidentMatchEventUseCase {
    override suspend fun invoke(
        event: MatchEventNotification,
        teamRemoteId: String,
        clubRemoteId: String,
    ) {
        try {
            val allMembers = clubMemberRepository.getClubMembers(clubRemoteId).first()
            val presidents =
                allMembers.filter { member ->
                    member.roles.any { role -> ClubRole.fromString(role) == ClubRole.PRESIDENT }
                }
            if (presidents.isEmpty()) return

            val eventType =
                when (event) {
                    is MatchEventNotification.Start -> NotificationEventType.MATCH_EVENTS
                    is MatchEventNotification.End -> NotificationEventType.MATCH_EVENTS
                    is MatchEventNotification.Goal -> NotificationEventType.GOALS
                }

            val payload = buildPayload(event)

            coroutineScope {
                presidents.map { president ->
                    async {
                        try {
                            val prefs =
                                notificationPreferencesRepository
                                    .getPreferences(president.userId, clubRemoteId)
                                    .first()
                            if (prefs.isEnabledFor(teamRemoteId, eventType)) {
                                fcmNotificationRepository.sendNotificationToUser(president.userId, payload)
                            }
                        } catch (e: Exception) {
                            // Don't let one president failure block others
                        }
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            // Fire-and-forget: notification failures must not affect match flow
        }
    }

    private fun buildPayload(event: MatchEventNotification): NotificationPayload {
        return when (event) {
            is MatchEventNotification.Start ->
                NotificationPayload.Typed.MatchStart(event.teamName, event.opponent)
            is MatchEventNotification.End ->
                NotificationPayload.Typed.MatchEnd(event.teamName, event.opponent, event.teamGoals, event.opponentGoals)
            is MatchEventNotification.Goal ->
                NotificationPayload.Typed.GoalScored(event.teamName, event.teamGoals, event.opponentGoals, event.minuteOfPlay)
        }
    }
}
