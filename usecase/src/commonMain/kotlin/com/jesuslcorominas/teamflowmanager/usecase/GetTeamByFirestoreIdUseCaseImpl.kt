package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamByFirestoreIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

internal class GetTeamByFirestoreIdUseCaseImpl(
    private val teamRepository: TeamRepository,
) : GetTeamByFirestoreIdUseCase {
    override suspend fun invoke(teamFirestoreId: String): Team? = teamRepository.getTeamByFirestoreId(teamFirestoreId)
}
