package com.jesuslcorominas.teamflowmanager.domain.utils

interface DatabaseImporter {
    suspend fun importDatabase(fileUri: String): Boolean
}
