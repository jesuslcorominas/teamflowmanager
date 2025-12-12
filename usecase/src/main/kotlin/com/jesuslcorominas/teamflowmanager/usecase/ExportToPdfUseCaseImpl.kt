package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.PdfExporter



internal class ExportToPdfUseCaseImpl(private val pdfExporter: PdfExporter) : ExportToPdfUseCase {
    override suspend fun invoke(exportData: ExportData, teamName: String): String? =
        pdfExporter.exportToPdf(exportData, teamName)
}
