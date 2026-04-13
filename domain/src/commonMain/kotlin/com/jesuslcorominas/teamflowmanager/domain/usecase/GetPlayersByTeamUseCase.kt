package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface GetPlayersByTeamUseCase {
    operator fun invoke(teamId: String): Flow<List<Player>>
}
