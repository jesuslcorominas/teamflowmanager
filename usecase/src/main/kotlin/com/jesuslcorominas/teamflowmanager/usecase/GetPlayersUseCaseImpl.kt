package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

internal class GetPlayersUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : GetPlayersUseCase {
    override fun invoke(): Flow<List<Player>> = playerRepository.getAllPlayers()
}
