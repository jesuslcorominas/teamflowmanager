package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats
import kotlinx.coroutines.flow.Flow

interface GetPlayerGoalStatsUseCase {
    operator fun invoke(): Flow<List<PlayerGoalStats>>
}
