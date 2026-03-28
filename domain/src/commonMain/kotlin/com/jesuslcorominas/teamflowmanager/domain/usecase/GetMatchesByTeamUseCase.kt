package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface GetMatchesByTeamUseCase {
    operator fun invoke(teamFirestoreId: String): Flow<List<Match>>
}
