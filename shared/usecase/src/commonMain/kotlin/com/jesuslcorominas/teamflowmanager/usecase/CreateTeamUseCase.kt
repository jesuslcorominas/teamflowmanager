package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

interface CreateTeamUseCase {
    suspend operator fun invoke(team: Team)
}

internal class CreateTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
) : CreateTeamUseCase {
    override suspend fun invoke(team: Team) {
        teamRepository.createTeam(team)
    }
}
