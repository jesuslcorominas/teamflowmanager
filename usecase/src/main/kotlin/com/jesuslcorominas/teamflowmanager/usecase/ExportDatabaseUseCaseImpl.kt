package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter



internal class ExportDatabaseUseCaseImpl(
    private val databaseExporter: DatabaseExporter,
) : ExportDatabaseUseCase {
    override suspend fun invoke(): String? {
        return databaseExporter.exportDatabase()
    }
}
