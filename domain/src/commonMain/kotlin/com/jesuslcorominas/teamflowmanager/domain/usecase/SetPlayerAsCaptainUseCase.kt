package com.jesuslcorominas.teamflowmanager.domain.usecase

interface SetPlayerAsCaptainUseCase {
    suspend operator fun invoke(playerId: Long)
}
