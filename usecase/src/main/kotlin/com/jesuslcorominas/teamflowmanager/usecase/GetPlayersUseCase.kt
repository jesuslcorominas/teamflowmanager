package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case interface for getting all players
 */
interface GetPlayersUseCase {
    operator fun invoke(): Flow<List<Player>>
}

/**
 * Implementation of GetPlayersUseCase
 */
internal class GetPlayersUseCaseImpl(
    private val playerRepository: PlayerRepository
) : GetPlayersUseCase {
    override fun invoke(): Flow<List<Player>> {
        return playerRepository.getAllPlayers()
    }
}
