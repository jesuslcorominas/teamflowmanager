package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

interface DeletePlayerUseCase {
    suspend operator fun invoke(playerId: Long)
}

internal class DeletePlayerUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : DeletePlayerUseCase {
    override suspend fun invoke(playerId: Long) {
        playerRepository.deletePlayer(playerId)
    }
}
