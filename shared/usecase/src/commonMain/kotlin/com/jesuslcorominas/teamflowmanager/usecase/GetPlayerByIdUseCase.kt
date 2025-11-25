package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

interface GetPlayerByIdUseCase {
    suspend operator fun invoke(playerId: Long): Player?
}

internal class GetPlayerByIdUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : GetPlayerByIdUseCase {
    override suspend fun invoke(playerId: Long): Player? = playerRepository.getPlayerById(playerId)
}
