package com.jesuslcorominas.teamflowmanager.usecase.impl

import android.content.Context
import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import com.jesuslcorominas.teamflowmanager.ui.util.PdfExporter
import com.jesuslcorominas.teamflowmanager.usecase.ExportToPdfUseCase

internal class ExportToPdfUseCaseImpl(
    private val context: Context,
) : ExportToPdfUseCase {
    override suspend fun invoke(exportData: ExportData, teamName: String): String? {
        val pdfExporter = PdfExporter(context)
        return pdfExporter.exportToPdf(exportData, teamName)?.toString()
    }
}
