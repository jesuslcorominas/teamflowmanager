package com.jesuslcorominas.teamflowmanager.domain.usecase

interface UnarchiveMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}
