package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData

interface ExportToPdfUseCase {
    suspend operator fun invoke(
        exportData: ExportData,
        teamName: String,
    ): String?
}
