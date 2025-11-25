package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter

interface ExportDatabaseUseCase {
    suspend operator fun invoke(): String?
}

internal class ExportDatabaseUseCaseImpl(
    private val databaseExporter: DatabaseExporter,
) : ExportDatabaseUseCase {
    override suspend fun invoke(): String? {
        return databaseExporter.exportDatabase()
    }
}
