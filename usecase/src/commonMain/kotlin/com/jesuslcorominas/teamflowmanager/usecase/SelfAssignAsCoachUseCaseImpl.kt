package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class SelfAssignAsCoachUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : SelfAssignAsCoachUseCase {
    override suspend fun invoke(teamId: String): Team {
        // Validate input
        require(teamId.isNotBlank()) {
            "Team ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated to self-assign as coach")

        // Get the team to verify it has no coach
        val team =
            teamRepository.getTeamById(teamId)
                ?: throw IllegalArgumentException("Team not found: $teamId")

        // Verify team belongs to a club
        require(team.clubRemoteId != null) {
            "Team must belong to a club to assign a coach"
        }

        // Verify team doesn't already have a coach
        require(team.coachId == null) {
            "Team already has a coach assigned. Cannot self-assign."
        }

        // Get current user's club membership
        val currentUserMembership =
            clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
                ?: throw IllegalStateException("User must be a club member to self-assign as coach")

        // Verify current user is a President
        require(currentUserMembership.hasRole(ClubRole.PRESIDENT)) {
            "Only club Presidents can self-assign as coach"
        }

        // Verify they are in the same club
        require(currentUserMembership.clubRemoteId == team.clubRemoteId) {
            "User and team must be in the same club"
        }

        try {
            // Update the team's coachId
            teamRepository.updateTeamCoachId(
                teamId = teamId,
                coachId = currentUser.id,
            )

            // Add Coach role to the President's roles (if not already present)
            if (!currentUserMembership.hasRole(ClubRole.COACH)) {
                team.clubRemoteId?.let { clubId ->
                    clubMemberRepository.addClubMemberRole(
                        userId = currentUser.id,
                        clubId = clubId,
                        role = ClubRole.COACH.roleName,
                    )
                }
            }

            // Return updated team
            return team.copy(coachId = currentUser.id)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to self-assign as coach: ${e.message}", e)
        }
    }
}
