package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club

interface UpdateClubUseCase {
    suspend operator fun invoke(
        id: String,
        name: String,
        homeGround: String?,
    ): Club
}
