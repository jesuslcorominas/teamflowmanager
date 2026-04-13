package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearTeamCoachUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

internal class ClearTeamCoachUseCaseImpl(
    private val teamRepository: TeamRepository,
) : ClearTeamCoachUseCase {
    override suspend fun invoke(teamId: String) = teamRepository.clearTeamCoach(teamId)
}
