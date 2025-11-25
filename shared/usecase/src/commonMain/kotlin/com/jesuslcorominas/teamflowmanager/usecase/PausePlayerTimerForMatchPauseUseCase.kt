package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

interface PausePlayerTimerForMatchPauseUseCase {
    suspend operator fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    )
}

internal class PausePlayerTimerForMatchPauseUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
    private val transactionRunner: TransactionRunner,
) : PausePlayerTimerForMatchPauseUseCase {
    override suspend fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        transactionRunner.run {
            playerTimeRepository.pauseTimerForMatchPause(playerId, currentTimeMillis)
        }
    }
}
