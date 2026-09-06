package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerGoalStatsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPlayerGoalStatsUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var useCase: GetPlayerGoalStatsUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        goalRepository = mockk()
        useCase = GetPlayerGoalStatsUseCaseImpl(playerRepository, goalRepository)
    }

    @Test
    fun `invoke should return empty stats when no players`() = runTest {
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should return correct goal count and matches for each player`() = runTest {
        val player1 = createPlayer("1", 10)
        val player2 = createPlayer("2", 7)
        val goals = listOf(
            Goal(id = "1", matchId = "1", scorerId = "1", goalTimeMillis = 100L, matchElapsedTimeMillis = 100L, isOpponentGoal = false),
            Goal(id = "2", matchId = "1", scorerId = "1", goalTimeMillis = 200L, matchElapsedTimeMillis = 200L, isOpponentGoal = false),
            Goal(id = "3", matchId = "2", scorerId = "1", goalTimeMillis = 300L, matchElapsedTimeMillis = 300L, isOpponentGoal = false),
            Goal(id = "4", matchId = "1", scorerId = "2", goalTimeMillis = 400L, matchElapsedTimeMillis = 400L, isOpponentGoal = false),
        )
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { goalRepository.getAllTeamGoals() } returns flowOf(goals)

        val result = useCase.invoke().first()

        assertEquals(2, result.size)
        // Sorted descending by total goals: player1 (3 goals) first
        val stats1 = result.find { it.player.id == "1" }!!
        assertEquals(3, stats1.totalGoals)
        assertEquals(2, stats1.matchesWithGoals) // distinct matches: 1, 2

        val stats2 = result.find { it.player.id == "2" }!!
        assertEquals(1, stats2.totalGoals)
        assertEquals(1, stats2.matchesWithGoals)
    }

    @Test
    fun `invoke should return zero goals for player with no goals`() = runTest {
        val player = createPlayer("1", 10)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player))
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertEquals(1, result.size)
        assertEquals(0, result[0].totalGoals)
        assertEquals(0, result[0].matchesWithGoals)
    }

    private fun createPlayer(id: String, number: Int) = Player(
        id = id,
        firstName = "Player",
        lastName = "Test",
        number = number,
        positions = listOf(Position.Forward),
        teamId = "1",
        isCaptain = false,
    )
}
