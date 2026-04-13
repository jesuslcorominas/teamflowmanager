package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team

interface GetTeamByIdUseCase {
    suspend operator fun invoke(teamId: String): Team?
}
