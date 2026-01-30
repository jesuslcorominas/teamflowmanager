package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class AssignCoachToTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : AssignCoachToTeamUseCase {

    override suspend fun invoke(teamFirestoreId: String, coachUserId: String): Team {
        // Validate inputs
        require(teamFirestoreId.isNotBlank()) {
            "Team Firestore ID cannot be blank"
        }
        require(coachUserId.isNotBlank()) {
            "Coach user ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser = getCurrentUser().first()
            ?: throw IllegalStateException("User must be authenticated to assign a coach")

        // Get the team
        val team = teamRepository.getTeamByFirestoreId(teamFirestoreId)
            ?: throw IllegalArgumentException("Team not found with Firestore ID: $teamFirestoreId")

        // Verify team belongs to a club
        require(team.clubFirestoreId != null) {
            "Team must belong to a club to assign a coach"
        }

        // Get current user's club membership
        val currentUserMembership = clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
            ?: throw IllegalStateException("User must be a club member to assign a coach")

        // Verify current user is a President
        require(currentUserMembership.role == ClubRole.PRESIDENT.roleName) {
            "Only club Presidents can assign coaches to teams"
        }

        // Verify they are in the same club
        require(currentUserMembership.clubFirestoreId == team.clubFirestoreId) {
            "User and team must be in the same club"
        }

        // Get the coach's club membership
        val coachMembership = clubMemberRepository.getClubMemberByUserIdAndClub(
            userId = coachUserId,
            clubFirestoreId = team.clubFirestoreId!!
        ) ?: throw IllegalArgumentException("Coach must be a member of the club")

        try {
            // TODO: Implement Firestore transaction or batch write for atomicity
            // Currently, these are two separate operations which could fail independently
            // causing data inconsistency. This should be refactored to use Firestore
            // batch writes or transactions to ensure both operations succeed or fail together.
            
            // Step 1: Update team's coachId
            teamRepository.updateTeamCoachId(
                teamFirestoreId = teamFirestoreId,
                coachId = coachUserId
            )

            // Step 2: Update club member's role to Coach
            clubMemberRepository.updateClubMemberRole(
                userId = coachUserId,
                clubFirestoreId = team.clubFirestoreId!!,
                role = ClubRole.COACH.roleName
            )

            // Return updated team
            return team.copy(coachId = coachUserId)
        } catch (e: Exception) {
            // If role update failed but team coachId was updated, we have inconsistent data
            throw IllegalStateException("Failed to assign coach: ${e.message}", e)
        }
    }
}
