package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ImportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter


internal class ImportDatabaseUseCaseImpl(
    private val databaseImporter: DatabaseImporter,
) : ImportDatabaseUseCase {
    override suspend fun invoke(fileUri: String): Boolean {
        return databaseImporter.importDatabase(fileUri)
    }
}
