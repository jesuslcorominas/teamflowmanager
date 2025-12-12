package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.flow.Flow

interface GetMatchSubstitutionsUseCase {
    operator fun invoke(matchId: Long): Flow<List<PlayerSubstitution>>
}
