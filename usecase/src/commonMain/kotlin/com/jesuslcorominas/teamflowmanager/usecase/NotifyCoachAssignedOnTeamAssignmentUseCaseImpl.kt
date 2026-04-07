package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository

internal class NotifyCoachAssignedOnTeamAssignmentUseCaseImpl(
    private val fcmNotificationRepository: FcmNotificationRepository,
) : NotifyCoachAssignedOnTeamAssignmentUseCase {
    override suspend fun invoke(
        coachUserId: String,
        assignedByUserId: String,
        teamName: String,
    ) {
        if (assignedByUserId == coachUserId) return

        fcmNotificationRepository.sendNotificationToUser(
            userId = coachUserId,
            payload = NotificationPayload.Typed.AssignedAsCoach(teamName = teamName),
        )
    }
}
