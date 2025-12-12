package com.jesuslcorominas.teamflowmanager.domain.usecase

interface ExportDatabaseUseCase {
    suspend operator fun invoke(): String?
}
