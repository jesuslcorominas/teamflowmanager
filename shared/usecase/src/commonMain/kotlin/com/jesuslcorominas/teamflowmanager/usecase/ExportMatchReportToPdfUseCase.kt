package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter

interface ExportMatchReportToPdfUseCase {
    suspend operator fun invoke(matchReportData: MatchReportData): String?
}

internal class ExportMatchReportToPdfUseCaseImpl(
    private val matchReportPdfExporter: MatchReportPdfExporter
) : ExportMatchReportToPdfUseCase {
    override suspend fun invoke(matchReportData: MatchReportData): String? =
        matchReportPdfExporter.exportMatchReportToPdf(matchReportData)
}
