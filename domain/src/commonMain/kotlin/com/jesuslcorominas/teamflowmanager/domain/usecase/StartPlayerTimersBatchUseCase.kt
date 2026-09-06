package com.jesuslcorominas.teamflowmanager.domain.usecase

interface StartPlayerTimersBatchUseCase {
    suspend operator fun invoke(
        matchId: String,
        playerIds: List<String>,
        currentTimeMillis: Long,
    )
}
