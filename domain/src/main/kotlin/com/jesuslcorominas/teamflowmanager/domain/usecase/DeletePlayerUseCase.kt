package com.jesuslcorominas.teamflowmanager.domain.usecase

interface DeletePlayerUseCase {
    suspend operator fun invoke(playerId: Long)
}
