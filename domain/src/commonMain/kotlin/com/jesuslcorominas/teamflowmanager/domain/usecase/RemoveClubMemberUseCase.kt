package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RemoveClubMemberUseCase {
    suspend operator fun invoke(
        userId: String,
        clubId: String,
    )
}
