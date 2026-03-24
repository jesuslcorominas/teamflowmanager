package com.jesuslcorominas.teamflowmanager.domain.usecase

interface StartPlayerTimersBatchUseCase {
    suspend operator fun invoke(matchId: Long, playerIds: List<Long>, currentTimeMillis: Long)
}
