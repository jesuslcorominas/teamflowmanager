package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player

interface UpdatePlayerUseCase {
    suspend operator fun invoke(player: Player)
}
