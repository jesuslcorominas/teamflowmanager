package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

interface UpdateTeamUseCase {
    suspend operator fun invoke(team: Team)
}

internal class UpdateTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
) : UpdateTeamUseCase {
    override suspend fun invoke(team: Team) {
        teamRepository.updateTeam(team)
    }
}
