package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.CoachAssignmentNotificationRepository

internal class NotifyCoachAssignedOnTeamAssignmentUseCaseImpl(
    private val coachAssignmentNotificationRepository: CoachAssignmentNotificationRepository,
) : NotifyCoachAssignedOnTeamAssignmentUseCase {
    override suspend fun invoke(
        coachUserId: String,
        assignedByUserId: String,
        teamName: String,
    ) {
        // Self-assignment: president assigns themselves as coach — no notification
        if (assignedByUserId == coachUserId) return

        coachAssignmentNotificationRepository.notifyCoachAssigned(
            coachUserId = coachUserId,
            teamName = teamName,
        )
    }
}
