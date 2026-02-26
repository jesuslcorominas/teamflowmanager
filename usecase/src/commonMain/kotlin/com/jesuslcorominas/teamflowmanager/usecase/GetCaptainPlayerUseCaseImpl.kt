package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository



internal class GetCaptainPlayerUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : GetCaptainPlayerUseCase {
    override suspend fun invoke(): Player? = playerRepository.getCaptainPlayer()
}
