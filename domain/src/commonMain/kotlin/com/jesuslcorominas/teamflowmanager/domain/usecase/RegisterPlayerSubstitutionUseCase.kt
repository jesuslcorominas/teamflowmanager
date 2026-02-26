package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RegisterPlayerSubstitutionUseCase {
    suspend operator fun invoke(
        matchId: Long,
        playerOutId: Long,
        playerInId: Long,
        currentTimeMillis: Long,
    )
}
