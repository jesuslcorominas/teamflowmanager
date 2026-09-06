package com.jesuslcorominas.teamflowmanager.ui.matches

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class MatchScreenAggregateScorersTest {
    private fun player(id: String) =
        Player(
            id = id,
            firstName = "First$id",
            lastName = "Last$id",
            number = 1,
            positions = listOf(Position.Midfielder),
            teamId = "team-1",
            isCaptain = false,
        )

    private fun goal(
        scorer: Player?,
        isOpponentGoal: Boolean = false,
        isOwnGoal: Boolean = false,
    ) = TimelineEvent.GoalScored(
        matchElapsedTimeMillis = 0L,
        scorer = scorer,
        isOpponentGoal = isOpponentGoal,
        isOwnGoal = isOwnGoal,
        teamScore = 1,
        opponentScore = 0,
    )

    @Test
    fun `goal with null scorer is counted under unknown scorer bucket instead of dropped`() {
        val events = listOf(goal(scorer = null))

        val result = aggregateScorers(events)

        assertEquals(emptyList(), result.scorers)
        assertEquals(0, result.ownGoalCount)
        assertEquals(1, result.unknownScorerCount)
    }

    @Test
    fun `total goals across buckets reconciles with the number of non-opponent goals`() {
        val playerA = player("a")
        val events =
            listOf(
                goal(scorer = playerA),
                goal(scorer = null),
                goal(scorer = null, isOwnGoal = true),
                goal(scorer = null, isOpponentGoal = true),
            )

        val result = aggregateScorers(events)

        val totalCountedNonOpponentGoals =
            result.scorers.sumOf { it.count } + result.ownGoalCount + result.unknownScorerCount
        assertEquals(3, totalCountedNonOpponentGoals)
        assertEquals(1, result.unknownScorerCount)
        assertEquals(1, result.ownGoalCount)
        assertEquals(listOf(ScorerEntry("${playerA.firstName} ${playerA.lastName}", 1)), result.scorers)
    }

    @Test
    fun `known scorers are still aggregated by player id as before`() {
        val playerA = player("a")
        val events = listOf(goal(scorer = playerA), goal(scorer = playerA))

        val result = aggregateScorers(events)

        assertEquals(1, result.scorers.size)
        assertEquals(2, result.scorers.first().count)
        assertEquals(0, result.unknownScorerCount)
    }

    @Test
    fun `opponent goals are ignored entirely`() {
        val events = listOf(goal(scorer = null, isOpponentGoal = true))

        val result = aggregateScorers(events)

        assertEquals(emptyList(), result.scorers)
        assertEquals(0, result.ownGoalCount)
        assertEquals(0, result.unknownScorerCount)
    }
}
