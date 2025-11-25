package com.jesuslcorominas.teamflowmanager.domain.utils

interface DatabaseExporter {
    suspend fun exportDatabase(): String?
}
