package com.jesuslcorominas.teamflowmanager.domain.utils

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData

interface PdfExporter {
    fun exportToPdf(exportData: ExportData, teamName: String): String?
}
