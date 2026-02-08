package com.jesuslcorominas.teamflowmanager.domain.usecase

/**
 * Use case for generating a team invitation link.
 * This creates a shareable link that allows a user to accept a team as coach.
 */
interface GenerateTeamInvitationUseCase {
    /**
     * Generate a shareable invitation link for a team.
     *
     * @param teamFirestoreId The Firestore document ID of the team
     * @param teamName The name of the team (for display in invitation)
     * @return A shareable deep link URL for the team invitation
     * @throws IllegalArgumentException if team not found
     * @throws IllegalStateException if user is not authenticated or not a President
     */
    suspend operator fun invoke(teamFirestoreId: String, teamName: String): String
}
