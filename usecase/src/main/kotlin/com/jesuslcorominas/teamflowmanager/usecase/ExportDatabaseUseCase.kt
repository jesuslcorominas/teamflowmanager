package com.jesuslcorominas.teamflowmanager.usecase

import android.content.Context
import android.net.Uri
import com.jesuslcorominas.teamflowmanager.usecase.repository.DatabaseRepository

interface ExportDatabaseUseCase {
    suspend operator fun invoke(context: Context, uri: Uri)
}

internal class ExportDatabaseUseCaseImpl(
    private val databaseRepository: DatabaseRepository,
) : ExportDatabaseUseCase {
    override suspend fun invoke(context: Context, uri: Uri) {
        databaseRepository.exportDatabase(context, uri)
    }
}
