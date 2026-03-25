package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData

interface ExportMatchReportToPdfUseCase {
    suspend operator fun invoke(matchReportData: MatchReportData): String?
}
