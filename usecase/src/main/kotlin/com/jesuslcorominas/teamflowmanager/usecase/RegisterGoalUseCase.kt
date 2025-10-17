package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.first

interface RegisterGoalUseCase {
    suspend operator fun invoke(
        matchId: Long,
        scorerId: Long,
        currentTimeMillis: Long,
        isOpponentGoal: Boolean = false,
    ): Long
}

internal class RegisterGoalUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val goalRepository: GoalRepository,
) : RegisterGoalUseCase {
    override suspend fun invoke(
        matchId: Long,
        scorerId: Long,
        currentTimeMillis: Long,
        isOpponentGoal: Boolean,
    ): Long {
        // Get match to calculate elapsed time
        val match = matchRepository.getMatch().first()
        requireNotNull(match) { "No active match found" }

        val matchElapsedTime = if (match.isRunning && match.lastStartTimeMillis != null) {
            match.elapsedTimeMillis + (currentTimeMillis - (match.lastStartTimeMillis ?: 0L))
        } else {
            match.elapsedTimeMillis
        }

        // Record the goal
        val goal = Goal(
            matchId = matchId,
            scorerId = scorerId,
            goalTimeMillis = currentTimeMillis,
            matchElapsedTimeMillis = matchElapsedTime,
            isOpponentGoal = isOpponentGoal,
        )

        return goalRepository.insertGoal(goal)
    }
}
