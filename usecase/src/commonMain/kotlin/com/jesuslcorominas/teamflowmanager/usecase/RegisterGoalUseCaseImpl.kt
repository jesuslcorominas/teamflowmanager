package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.first

internal class RegisterGoalUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val goalRepository: GoalRepository,
) : RegisterGoalUseCase {
    override suspend fun invoke(
        matchId: Long,
        scorerId: Long?,
        currentTimeMillis: Long,
        isOpponentGoal: Boolean,
        isOwnGoal: Boolean,
    ): Long {
        // Get match to calculate elapsed time
        val match = matchRepository.getMatchById(matchId).first()
        requireNotNull(match) { "No active match found" }

        val matchElapsedTime = match.getTotalElapsed(currentTimeMillis)

        // Record the goal
        val goal =
            Goal(
                matchId = matchId,
                scorerId = scorerId,
                goalTimeMillis = currentTimeMillis,
                matchElapsedTimeMillis = matchElapsedTime,
                isOpponentGoal = isOpponentGoal,
                isOwnGoal = isOwnGoal,
            )

        val updatedMatch =
            match.copy(
                goals = if (isOpponentGoal) match.goals else match.goals + 1,
                opponentGoals = if (isOpponentGoal) match.opponentGoals + 1 else match.opponentGoals,
            )

        matchRepository.updateMatch(updatedMatch)

        return goalRepository.insertGoal(goal)
    }
}
