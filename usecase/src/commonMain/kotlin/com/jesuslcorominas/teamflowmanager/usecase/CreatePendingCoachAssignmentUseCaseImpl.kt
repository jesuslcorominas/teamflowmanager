package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreatePendingCoachAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingCoachAssignmentRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class CreatePendingCoachAssignmentUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val pendingCoachAssignmentRepository: PendingCoachAssignmentRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : CreatePendingCoachAssignmentUseCase {
    override suspend fun invoke(
        teamId: String,
        email: String,
    ) {
        require(teamId.isNotBlank()) { "Team ID cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }

        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated")

        val team =
            teamRepository.getTeamById(teamId)
                ?: throw IllegalArgumentException("Team not found: $teamId")

        val clubId =
            team.clubFirestoreId
                ?: throw IllegalArgumentException("Team must belong to a club")

        val membership =
            clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
                ?: throw IllegalStateException("User must be a club member")

        require(membership.hasRole(ClubRole.PRESIDENT)) {
            "Only club Presidents can create pending coach assignments"
        }

        require(membership.clubFirestoreId == clubId) {
            "User and team must be in the same club"
        }

        pendingCoachAssignmentRepository.create(teamId, clubId, email)
        teamRepository.updateTeamPendingCoachEmail(teamId, email)
    }
}
