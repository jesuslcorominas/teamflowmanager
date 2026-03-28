package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

internal class GetTeamByIdUseCaseImpl(
    private val teamRepository: TeamRepository,
) : GetTeamByIdUseCase {
    override suspend fun invoke(teamId: String): Team? = teamRepository.getTeamById(teamId)
}
