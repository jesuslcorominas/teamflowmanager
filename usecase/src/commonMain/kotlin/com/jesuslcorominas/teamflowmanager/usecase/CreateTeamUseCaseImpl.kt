package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class CreateTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
) : CreateTeamUseCase {
    override suspend fun invoke(team: Team) {
        val isPresident = getUserClubMembership().first()?.hasRole(ClubRole.PRESIDENT) ?: false
        val teamToCreate =
            if (!isPresident && team.coachId == null) {
                val currentUserId = getCurrentUser().first()?.id
                team.copy(coachId = currentUserId)
            } else {
                team
            }
        teamRepository.createTeam(teamToCreate)
    }
}
