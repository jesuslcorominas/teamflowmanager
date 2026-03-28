package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RegenerateInvitationCodeUseCase {
    suspend operator fun invoke(firestoreId: String): String
}
