package com.jesuslcorominas.teamflowmanager.domain.usecase

interface DeleteMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}
