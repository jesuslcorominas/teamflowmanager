package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.AcceptTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class AcceptTeamInvitationUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : AcceptTeamInvitationUseCase {
    override suspend fun invoke(teamId: String): Team {
        // Validate input
        require(teamId.isNotBlank()) {
            "Team ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated to accept team invitation")

        // Validate user data
        require(currentUser.displayName?.isNotBlank() == true) {
            "User display name is required to accept team invitation"
        }
        require(currentUser.email?.isNotBlank() == true) {
            "User email is required to accept team invitation"
        }

        // Get the team
        val team =
            teamRepository.getTeamById(teamId)
                ?: throw IllegalArgumentException("Team not found: $teamId")

        // Verify team has no coach yet
        require(team.coachId == null) {
            "Team already has a coach assigned"
        }

        // Verify team belongs to a club
        require(team.clubId != null && team.clubRemoteId != null) {
            "Team must belong to a club to accept invitation"
        }

        try {
            // TODO: Implement Firestore transaction or batch write for atomicity
            // Currently, these are two separate operations which could fail independently
            // causing data inconsistency. This should be refactored to use Firestore
            // batch writes or transactions to ensure both operations succeed or fail together.

            // Step 1: Update team's coachId
            teamRepository.updateTeamCoachId(
                teamId = teamId,
                coachId = currentUser.id,
            )

            // Step 2: Create or update club member with Coach role
            clubMemberRepository.createOrUpdateClubMember(
                userId = currentUser.id,
                name = currentUser.displayName!!,
                email = currentUser.email!!,
                clubNumericId = team.clubId!!,
                clubId = team.clubRemoteId!!,
                roles = listOf(ClubRole.COACH.roleName),
            )

            // Return updated team
            return team.copy(coachId = currentUser.id)
        } catch (e: Exception) {
            // If member creation failed but team coachId was updated, we have inconsistent data
            throw IllegalStateException("Failed to accept team invitation: ${e.message}", e)
        }
    }
}
