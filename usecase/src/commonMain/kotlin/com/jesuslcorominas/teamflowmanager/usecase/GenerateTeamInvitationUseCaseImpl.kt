package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class GenerateTeamInvitationUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : GenerateTeamInvitationUseCase {
    override suspend fun invoke(
        teamId: String,
        teamName: String,
    ): String {
        // Validate inputs
        require(teamId.isNotBlank()) {
            "Team ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated to generate team invitation")

        // Get the team
        val team =
            teamRepository.getTeamById(teamId)
                ?: throw IllegalArgumentException("Team not found: $teamId")

        // Verify team belongs to a club
        require(team.clubRemoteId != null) {
            "Team must belong to a club to generate invitation"
        }

        // Get current user's club membership
        val currentUserMembership =
            clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
                ?: throw IllegalStateException("User must be a club member to generate team invitation")

        // Verify current user is a President
        require(currentUserMembership.hasRole(ClubRole.PRESIDENT)) {
            "Only club Presidents can generate team invitations"
        }

        // Verify they are in the same club
        require(currentUserMembership.clubRemoteId == team.clubRemoteId) {
            "User and team must be in the same club"
        }

        // Generate invitation link using the repository (which delegates to data source)
        return teamRepository.generateTeamInvitationLink(teamId, teamName)
    }
}
