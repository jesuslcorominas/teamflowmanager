package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchSummary
import kotlinx.coroutines.flow.Flow

interface GetMatchSummaryUseCase {
    operator fun invoke(
        matchId: Long,
        teamId: String? = null,
    ): Flow<MatchSummary?>
}
