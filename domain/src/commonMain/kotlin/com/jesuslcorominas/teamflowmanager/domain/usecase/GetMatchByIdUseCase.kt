package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface GetMatchByIdUseCase {
    operator fun invoke(
        matchId: Long,
        teamId: String? = null,
    ): Flow<Match?>
}
