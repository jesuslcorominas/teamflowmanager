package com.jesuslcorominas.teamflowmanager.data.core.datasource

import android.content.Context
import android.net.Uri

interface DatabaseLocalDataSource {
    suspend fun exportDatabase(context: Context, uri: Uri)
    suspend fun importDatabase(context: Context, uri: Uri)
}
