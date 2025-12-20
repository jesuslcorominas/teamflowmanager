package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface GetArchivedMatchesUseCase {
    operator fun invoke(): Flow<List<Match>>
}
