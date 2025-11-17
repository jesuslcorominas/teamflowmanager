package com.jesuslcorominas.teamflowmanager.data.core.repository

import android.content.Context
import android.net.Uri
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DatabaseLocalDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.DatabaseRepository

internal class DatabaseRepositoryImpl(
    private val localDataSource: DatabaseLocalDataSource,
) : DatabaseRepository {
    override suspend fun exportDatabase(context: Context, uri: Uri) {
        localDataSource.exportDatabase(context, uri)
    }

    override suspend fun importDatabase(context: Context, uri: Uri) {
        localDataSource.importDatabase(context, uri)
    }
}
