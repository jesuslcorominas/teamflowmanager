package com.jesuslcorominas.teamflowmanager.domain.usecase

interface RegisterGoalUseCase {
    suspend operator fun invoke(
        matchId: String,
        scorerId: String?,
        currentTimeMillis: Long,
        isOpponentGoal: Boolean = false,
        isOwnGoal: Boolean = false,
    ): String
}
