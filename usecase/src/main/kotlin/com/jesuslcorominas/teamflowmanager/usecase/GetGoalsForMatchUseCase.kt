package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

interface GetGoalsForMatchUseCase {
    operator fun invoke(matchId: Long): Flow<List<Goal>>
}

internal class GetGoalsForMatchUseCaseImpl(
    private val goalRepository: GoalRepository,
) : GetGoalsForMatchUseCase {
    override fun invoke(matchId: Long): Flow<List<Goal>> =
        goalRepository.getMatchGoals(matchId)
}
