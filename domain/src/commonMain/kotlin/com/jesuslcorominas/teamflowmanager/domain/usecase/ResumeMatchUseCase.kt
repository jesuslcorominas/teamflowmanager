package com.jesuslcorominas.teamflowmanager.domain.usecase

interface ResumeMatchUseCase {
    suspend operator fun invoke(
        matchId: Long,
        currentTimeMillis: Long,
    )
}
