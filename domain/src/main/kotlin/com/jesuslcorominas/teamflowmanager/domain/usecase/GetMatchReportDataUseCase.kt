package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import kotlinx.coroutines.flow.Flow

interface GetMatchReportDataUseCase {
    operator fun invoke(matchId: Long): Flow<MatchReportData?>
}
