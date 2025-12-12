package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player

interface GetPlayerByIdUseCase {
    suspend operator fun invoke(playerId: Long): Player?
}
