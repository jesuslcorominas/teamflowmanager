package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository



internal class GetPlayerByIdUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : GetPlayerByIdUseCase {
    override suspend fun invoke(playerId: Long): Player? = playerRepository.getPlayerById(playerId)
}
