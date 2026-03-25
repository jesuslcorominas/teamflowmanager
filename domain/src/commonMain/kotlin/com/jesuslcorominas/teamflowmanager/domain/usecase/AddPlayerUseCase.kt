package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player

interface AddPlayerUseCase {
    suspend operator fun invoke(player: Player)
}
