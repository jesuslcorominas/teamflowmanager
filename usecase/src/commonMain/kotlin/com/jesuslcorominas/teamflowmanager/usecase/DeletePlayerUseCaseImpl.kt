package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

internal class DeletePlayerUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : DeletePlayerUseCase {
    override suspend fun invoke(playerId: Long) {
        playerRepository.deletePlayer(playerId)
    }
}
