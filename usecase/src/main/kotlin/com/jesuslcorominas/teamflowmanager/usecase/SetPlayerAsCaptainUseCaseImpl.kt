package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

internal class SetPlayerAsCaptainUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : SetPlayerAsCaptainUseCase {
    override suspend fun invoke(playerId: Long) {
        playerRepository.setPlayerAsCaptain(playerId)
    }
}
