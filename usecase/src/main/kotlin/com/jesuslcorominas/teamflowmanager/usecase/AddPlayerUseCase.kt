package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

interface AddPlayerUseCase {
    suspend operator fun invoke(player: Player)
}

internal class AddPlayerUseCaseImpl(
    private val playerRepository: PlayerRepository
) : AddPlayerUseCase {
    override suspend fun invoke(player: Player) {
        playerRepository.addPlayer(player)
    }
}
