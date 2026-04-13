package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class AssignCoachToTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val notifyCoachAssigned: NotifyCoachAssignedOnTeamAssignmentUseCase,
) : AssignCoachToTeamUseCase {
    override suspend fun invoke(
        teamId: String,
        coachUserId: String,
    ): Team {
        // Validate inputs
        require(teamId.isNotBlank()) {
            "Team ID cannot be blank"
        }
        require(coachUserId.isNotBlank()) {
            "Coach user ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated to assign a coach")

        // Get the team
        val team =
            teamRepository.getTeamById(teamId)
                ?: throw IllegalArgumentException("Team not found: $teamId")

        // Verify team belongs to a club
        require(team.clubRemoteId != null) {
            "Team must belong to a club to assign a coach"
        }

        // Get current user's club membership
        val currentUserMembership =
            clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
                ?: throw IllegalStateException("User must be a club member to assign a coach")

        // Verify current user is a President
        require(currentUserMembership.hasRole(ClubRole.PRESIDENT)) {
            "Only club Presidents can assign coaches to teams"
        }

        // Verify they are in the same club
        require(currentUserMembership.clubRemoteId == team.clubRemoteId) {
            "User and team must be in the same club"
        }

        // Get the coach's club membership
        val coachMembership =
            clubMemberRepository.getClubMemberByUserIdAndClub(
                userId = coachUserId,
                clubId = team.clubRemoteId!!,
            ) ?: throw IllegalArgumentException("Coach must be a member of the club")

        try {
            // TODO: Implement Firestore transaction or batch write for atomicity
            // Currently, these are two separate operations which could fail independently
            // causing data inconsistency. This should be refactored to use Firestore
            // batch writes or transactions to ensure both operations succeed or fail together.

            // Step 1: Update team's coachId
            teamRepository.updateTeamCoachId(
                teamId = teamId,
                coachId = coachUserId,
            )

            // Step 2: Add Coach role to club member (if not already present)
            if (!coachMembership.hasRole(ClubRole.COACH)) {
                clubMemberRepository.addClubMemberRole(
                    userId = coachUserId,
                    clubId = team.clubRemoteId!!,
                    role = ClubRole.COACH.roleName,
                )
            }

            // Step 3: Notify the assigned coach (fire-and-forget; errors are swallowed
            //          to avoid rolling back a successful assignment due to a notification failure)
            try {
                notifyCoachAssigned(
                    coachUserId = coachUserId,
                    assignedByUserId = currentUser.id,
                    teamName = team.name,
                )
            } catch (_: Exception) {
                // Notification failures must not break the assignment flow
            }

            // Return updated team
            return team.copy(coachId = coachUserId)
        } catch (e: Exception) {
            // If role update failed but team coachId was updated, we have inconsistent data
            throw IllegalStateException("Failed to assign coach: ${e.message}", e)
        }
    }
}
