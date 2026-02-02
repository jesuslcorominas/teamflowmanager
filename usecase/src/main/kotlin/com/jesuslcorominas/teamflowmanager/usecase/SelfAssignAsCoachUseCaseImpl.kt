package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class SelfAssignAsCoachUseCaseImpl(
    private val assignCoachToTeam: AssignCoachToTeamUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val teamRepository: TeamRepository,
) : SelfAssignAsCoachUseCase {

    override suspend fun invoke(teamFirestoreId: String): Team {
        // Validate input
        require(teamFirestoreId.isNotBlank()) {
            "Team Firestore ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser = getCurrentUser().first()
            ?: throw IllegalStateException("User must be authenticated to self-assign as coach")

        // Get the team to verify it has no coach
        val team = teamRepository.getTeamByFirestoreId(teamFirestoreId)
            ?: throw IllegalArgumentException("Team not found with Firestore ID: $teamFirestoreId")

        // Verify team doesn't already have a coach
        require(team.coachId == null) {
            "Team already has a coach assigned. Cannot self-assign."
        }

        // Use existing AssignCoachToTeamUseCase which handles all validations
        // (President role check, club membership, atomic updates, etc.)
        return assignCoachToTeam(teamFirestoreId, currentUser.id)
    }
}
