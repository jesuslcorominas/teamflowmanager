package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team

/**
 * Use case for a President to self-assign as coach to a team.
 * This operation updates the team's coachId field with the current user's ID
 * and adds "Coach" to their roles list.
 * 
 * **Important**: When a President self-assigns as coach, their clubMember.roles 
 * gains "Coach" while maintaining "Presidente" (roles become ["Presidente", "Coach"]). 
 * This preserves their administrative privileges while allowing them to coach the team.
 * 
 * Only club Presidents can perform this operation, and only on teams without a coach.
 */
interface SelfAssignAsCoachUseCase {
    /**
     * Self-assign the current user as coach to a team.
     * 
     * @param teamFirestoreId The Firestore document ID of the team
     * @return The updated Team with the new coachId
     * @throws IllegalArgumentException if team not found or team already has a coach
     * @throws IllegalStateException if user is not authenticated or not a President
     */
    suspend operator fun invoke(teamFirestoreId: String): Team
}
