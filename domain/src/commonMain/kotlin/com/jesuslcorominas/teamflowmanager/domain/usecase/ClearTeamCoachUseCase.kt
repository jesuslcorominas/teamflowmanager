package com.jesuslcorominas.teamflowmanager.domain.usecase

interface ClearTeamCoachUseCase {
    suspend operator fun invoke(teamId: String)
}
