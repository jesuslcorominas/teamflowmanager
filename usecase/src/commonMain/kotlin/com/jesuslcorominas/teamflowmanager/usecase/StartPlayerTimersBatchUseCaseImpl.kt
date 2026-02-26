package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

internal class StartPlayerTimersBatchUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : StartPlayerTimersBatchUseCase {
    override suspend fun invoke(playerIds: List<Long>, currentTimeMillis: Long) {
        playerTimeRepository.startTimersBatch(playerIds, currentTimeMillis)
    }
}
