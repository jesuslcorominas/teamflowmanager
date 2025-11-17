package com.jesuslcorominas.teamflowmanager.usecase.repository

import android.content.Context
import android.net.Uri

interface DatabaseRepository {
    suspend fun exportDatabase(context: Context, uri: Uri)
    suspend fun importDatabase(context: Context, uri: Uri)
}
