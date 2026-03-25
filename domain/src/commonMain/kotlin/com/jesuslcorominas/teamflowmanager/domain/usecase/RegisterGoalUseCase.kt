package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RegisterGoalUseCase {
    suspend operator fun invoke(
        matchId: Long,
        scorerId: Long?,
        currentTimeMillis: Long,
        isOpponentGoal: Boolean = false,
        isOwnGoal: Boolean = false,
    ): Long
}
