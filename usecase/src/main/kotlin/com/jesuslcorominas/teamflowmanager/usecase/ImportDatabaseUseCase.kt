package com.jesuslcorominas.teamflowmanager.usecase

import android.content.Context
import android.net.Uri
import com.jesuslcorominas.teamflowmanager.usecase.repository.DatabaseRepository

interface ImportDatabaseUseCase {
    suspend operator fun invoke(context: Context, uri: Uri)
}

internal class ImportDatabaseUseCaseImpl(
    private val databaseRepository: DatabaseRepository,
) : ImportDatabaseUseCase {
    override suspend fun invoke(context: Context, uri: Uri) {
        databaseRepository.importDatabase(context, uri)
    }
}
