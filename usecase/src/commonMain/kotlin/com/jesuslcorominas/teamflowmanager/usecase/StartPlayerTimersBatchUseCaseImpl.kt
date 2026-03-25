package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

internal class StartPlayerTimersBatchUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : StartPlayerTimersBatchUseCase {
    override suspend fun invoke(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
    ) {
        playerTimeRepository.startTimersBatch(matchId, playerIds, currentTimeMillis)
    }
}
