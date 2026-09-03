package com.jesuslcorominas.teamflowmanager.domain.usecase

interface PausePlayerTimerForMatchPauseUseCase {
    suspend operator fun invoke(
        playerId: String,
        currentTimeMillis: Long,
    )
}
