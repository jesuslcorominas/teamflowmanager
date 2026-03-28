package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team

interface GetTeamByFirestoreIdUseCase {
    suspend operator fun invoke(teamFirestoreId: String): Team?
}
