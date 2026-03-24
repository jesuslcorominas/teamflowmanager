package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import kotlinx.coroutines.flow.Flow

interface GetPlayerTimeStatsUseCase {
    operator fun invoke(): Flow<List<PlayerTimeStats>>
}
