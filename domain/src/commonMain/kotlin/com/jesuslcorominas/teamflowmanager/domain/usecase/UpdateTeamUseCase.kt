package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team

interface UpdateTeamUseCase {
    suspend operator fun invoke(team: Team)
}
