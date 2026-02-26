package com.jesuslcorominas.teamflowmanager.domain.usecase

interface UpdateScheduledMatchesCaptainUseCase {
    suspend operator fun invoke(captainId: Long?)
}
