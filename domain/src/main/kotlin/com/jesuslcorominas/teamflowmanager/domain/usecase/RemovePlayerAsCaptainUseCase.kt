package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RemovePlayerAsCaptainUseCase {
    suspend operator fun invoke(playerId: Long)
}
