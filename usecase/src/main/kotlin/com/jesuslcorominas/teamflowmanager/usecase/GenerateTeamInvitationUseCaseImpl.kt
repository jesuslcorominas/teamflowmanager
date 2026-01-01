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

    companion object {
        private const val BASE_URL = "https://teamflowmanager.app"
        private const val TEAM_INVITATION_PATH = "/team/accept"
    }

    override suspend fun invoke(teamFirestoreId: String, teamName: String): String {
        // Validate inputs
        require(teamFirestoreId.isNotBlank()) {
            "Team Firestore ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser = getCurrentUser().first()
            ?: throw IllegalStateException("User must be authenticated to generate team invitation")

        // Get the team
        val team = teamRepository.getTeamByFirestoreId(teamFirestoreId)
            ?: throw IllegalArgumentException("Team not found with Firestore ID: $teamFirestoreId")

        // Verify team belongs to a club
        require(team.clubFirestoreId != null) {
            "Team must belong to a club to generate invitation"
        }

        // Get current user's club membership
        val currentUserMembership = clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
            ?: throw IllegalStateException("User must be a club member to generate team invitation")

        // Verify current user is a President
        require(currentUserMembership.role == ClubRole.PRESIDENT.roleName) {
            "Only club Presidents can generate team invitations"
        }

        // Verify they are in the same club
        require(currentUserMembership.clubFirestoreId == team.clubFirestoreId) {
            "User and team must be in the same club"
        }

        // Generate deep link URL with team ID
        return "$BASE_URL$TEAM_INVITATION_PATH?teamId=$teamFirestoreId&teamName=${teamName.replace(" ", "%20")}"
    }
}
