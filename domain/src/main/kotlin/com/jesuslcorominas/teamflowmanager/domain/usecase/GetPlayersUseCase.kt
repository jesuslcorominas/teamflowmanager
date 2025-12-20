package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface GetPlayersUseCase {
    operator fun invoke(): Flow<List<Player>>
}
