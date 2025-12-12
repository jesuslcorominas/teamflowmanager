package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import kotlinx.coroutines.flow.Flow

interface GetExportDataUseCase {
    operator fun invoke(): Flow<ExportData>
}
