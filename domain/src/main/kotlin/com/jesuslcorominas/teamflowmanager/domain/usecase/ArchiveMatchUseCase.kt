package com.jesuslcorominas.teamflowmanager.domain.usecase

interface ArchiveMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}
