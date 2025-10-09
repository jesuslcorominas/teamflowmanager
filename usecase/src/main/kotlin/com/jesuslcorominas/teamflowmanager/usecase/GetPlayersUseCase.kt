package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

interface GetPlayersUseCase {
    operator fun invoke(): Flow<List<Player>>
}

internal class GetPlayersUseCaseImpl(
    private val playerRepository: PlayerRepository
) : GetPlayersUseCase {
    override fun invoke(): Flow<List<Player>> {
        return playerRepository.getAllPlayers()
    }
}
