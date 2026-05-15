package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

internal class StartPlayerTimersBatchUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : StartPlayerTimersBatchUseCase {
    override suspend fun invoke(
        matchId: String,
        playerIds: List<String>,
        currentTimeMillis: Long,
    ) {
        playerTimeRepository.startTimersBatch(matchId, playerIds, currentTimeMillis)
    }
}
