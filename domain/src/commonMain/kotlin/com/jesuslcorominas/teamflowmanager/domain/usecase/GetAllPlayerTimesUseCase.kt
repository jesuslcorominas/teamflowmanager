package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface GetAllPlayerTimesUseCase {
    operator fun invoke(matchId: String): Flow<List<PlayerTime>>
}
