package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club

interface GetClubByIdUseCase {
    suspend operator fun invoke(id: String): Club?
}
