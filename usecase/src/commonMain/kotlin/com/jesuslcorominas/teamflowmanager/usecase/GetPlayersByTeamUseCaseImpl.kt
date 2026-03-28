package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

internal class GetPlayersByTeamUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : GetPlayersByTeamUseCase {
    override fun invoke(teamId: String): Flow<List<Player>> = playerRepository.getPlayersByTeam(teamId)
}
