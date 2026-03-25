package com.jesuslcorominas.teamflowmanager.domain.usecase
interface PausePlayerTimerForMatchPauseUseCase {
    suspend operator fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    )
}
