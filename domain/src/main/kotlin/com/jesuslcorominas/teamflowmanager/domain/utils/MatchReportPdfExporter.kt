package com.jesuslcorominas.teamflowmanager.domain.utils

import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData

interface MatchReportPdfExporter {
    fun exportMatchReportToPdf(matchReportData: MatchReportData): String?
}
