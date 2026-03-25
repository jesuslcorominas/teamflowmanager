package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

internal class GetTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
) : GetTeamUseCase {
    override fun invoke(): Flow<Team?> = teamRepository.getTeam()
}
