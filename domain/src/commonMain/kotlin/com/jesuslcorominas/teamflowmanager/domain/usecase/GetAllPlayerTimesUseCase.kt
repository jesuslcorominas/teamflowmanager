package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface GetAllPlayerTimesUseCase {
    operator fun invoke(matchId: Long, teamId: String? = null): Flow<List<PlayerTime>>
}
