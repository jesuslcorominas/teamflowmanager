package com.jesuslcorominas.teamflowmanager.domain.usecase

interface StartPlayerTimersBatchUseCase {
    suspend operator fun invoke(playerIds: List<Long>, currentTimeMillis: Long)
}
