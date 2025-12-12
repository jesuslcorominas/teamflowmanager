package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.HasLocalDataWithoutUserIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository


internal class HasLocalDataWithoutUserIdUseCaseImpl(
    private val teamRepository: TeamRepository,
) : HasLocalDataWithoutUserIdUseCase {
    override suspend fun invoke(): Boolean = teamRepository.hasLocalTeamWithoutUserId()
}
