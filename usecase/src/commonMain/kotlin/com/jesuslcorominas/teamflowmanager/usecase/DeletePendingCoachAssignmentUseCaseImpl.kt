package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePendingCoachAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingCoachAssignmentRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

internal class DeletePendingCoachAssignmentUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val pendingCoachAssignmentRepository: PendingCoachAssignmentRepository,
) : DeletePendingCoachAssignmentUseCase {
    override suspend fun invoke(teamId: String) {
        require(teamId.isNotBlank()) { "Team ID cannot be blank" }
        pendingCoachAssignmentRepository.delete(teamId)
        teamRepository.updateTeamPendingCoachEmail(teamId, null)
    }
}