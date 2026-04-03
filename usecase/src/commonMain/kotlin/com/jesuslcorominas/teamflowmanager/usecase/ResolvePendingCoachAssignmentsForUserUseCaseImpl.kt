package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResolvePendingCoachAssignmentsForUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingCoachAssignmentRepository

internal class ResolvePendingCoachAssignmentsForUserUseCaseImpl(
    private val pendingCoachAssignmentRepository: PendingCoachAssignmentRepository,
    private val assignCoachToTeam: AssignCoachToTeamUseCase,
) : ResolvePendingCoachAssignmentsForUserUseCase {
    override suspend fun invoke(
        userId: String,
        userEmail: String,
    ) {
        val pending = pendingCoachAssignmentRepository.getByEmail(userEmail)
        for (assignment in pending) {
            try {
                assignCoachToTeam(assignment.teamId, userId)
                pendingCoachAssignmentRepository.delete(assignment.teamId)
            } catch (_: Exception) {
                // Continue resolving others even if one fails
            }
        }
    }
}
