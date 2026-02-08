package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team

/**
 * Use case for assigning a coach to a team.
 * This operation updates the team's coachId field and the club member's role to "Coach".
 * Only club Presidents can perform this operation.
 */
interface AssignCoachToTeamUseCase {
    /**
     * Assign a coach (club member) to a team.
     *
     * @param teamFirestoreId The Firestore document ID of the team
     * @param coachUserId The Firebase user ID of the coach to assign
     * @return The updated Team with the new coachId
     * @throws IllegalArgumentException if team or coach not found
     * @throws IllegalStateException if user is not authenticated or not a President
     */
    suspend operator fun invoke(teamFirestoreId: String, coachUserId: String): Team
}
