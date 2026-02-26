package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository



internal class UpdatePlayerUseCaseImpl(
    private val playerRepository: PlayerRepository
) : UpdatePlayerUseCase {
    override suspend fun invoke(player: Player) {
        playerRepository.updatePlayer(player)
    }
}
