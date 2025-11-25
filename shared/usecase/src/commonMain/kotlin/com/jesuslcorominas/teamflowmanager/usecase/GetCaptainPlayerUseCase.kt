package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository

interface GetCaptainPlayerUseCase {
    suspend operator fun invoke(): Player?
}

internal class GetCaptainPlayerUseCaseImpl(
    private val playerRepository: PlayerRepository,
) : GetCaptainPlayerUseCase {
    override suspend fun invoke(): Player? = playerRepository.getCaptainPlayer()
}
