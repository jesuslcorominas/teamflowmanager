package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

internal class RemovePlayerAsCaptainUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : RemovePlayerAsCaptainUseCase {
    override suspend fun invoke(playerId: Long) {
        playerRepository.removePlayerAsCaptain(playerId)
    }
}
