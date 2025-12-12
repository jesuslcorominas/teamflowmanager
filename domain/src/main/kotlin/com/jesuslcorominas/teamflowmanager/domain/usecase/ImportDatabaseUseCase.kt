package com.jesuslcorominas.teamflowmanager.domain.usecase
interface ImportDatabaseUseCase {
    suspend operator fun invoke(fileUri: String): Boolean
}
