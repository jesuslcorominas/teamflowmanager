package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team

/**
 * Use case for accepting a team invitation.
 * This operation assigns the current user as coach of the team and updates their role.
 */
interface AcceptTeamInvitationUseCase {
    /**
     * Accept a team invitation and become the coach of the team.
     *
     * @param teamId The Firestore document ID of the team to accept
     * @return The updated Team with the user assigned as coach
     * @throws IllegalArgumentException if team not found or already has a coach
     * @throws IllegalStateException if user is not authenticated
     */
    suspend operator fun invoke(teamId: String): Team
}
