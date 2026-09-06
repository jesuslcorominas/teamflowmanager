package com.jesuslcorominas.teamflowmanager.domain.usecase

interface ResumeMatchUseCase {
    suspend operator fun invoke(
        matchId: String,
        currentTimeMillis: Long,
    )
}
