package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository

// TODO internacionalizar?

private const val NOTIFICATION_TITLE = "Has sido asignado como entrenador"

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
            title = NOTIFICATION_TITLE,
            body = "Has sido asignado como entrenador del equipo $teamName",
        )
    }
}
