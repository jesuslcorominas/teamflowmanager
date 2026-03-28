package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

internal class GetTeamsByClubUseCaseImpl(
    private val teamRepository: TeamRepository,
) : GetTeamsByClubUseCase {
    override fun invoke(clubId: String): Flow<List<Team>> {
        return teamRepository.getTeamsByClub(clubId)
    }
}
