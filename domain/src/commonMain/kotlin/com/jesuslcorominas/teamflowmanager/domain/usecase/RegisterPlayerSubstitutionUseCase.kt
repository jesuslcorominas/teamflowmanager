package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RegisterPlayerSubstitutionUseCase {
    suspend operator fun invoke(
        matchId: String,
        playerOutId: String,
        playerInId: String,
        currentTimeMillis: Long,
    )
}
