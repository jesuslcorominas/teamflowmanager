package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetExportDataUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetExportDataUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var useCase: GetExportDataUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        matchRepository = mockk()
        playerTimeHistoryRepository = mockk()
        goalRepository = mockk()
        useCase = GetExportDataUseCaseImpl(playerRepository, matchRepository, playerTimeHistoryRepository, goalRepository)
    }

    @Test
    fun `invoke should return empty export data when no data`() = runTest {
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertTrue(result.playerStats.isEmpty())
        assertTrue(result.topScorers.isEmpty())
        assertTrue(result.matchResults.isEmpty())
    }

    @Test
    fun `invoke should calculate correct player stats from time history and goals`() = runTest {
        val player = createPlayer(1L)
        val finishedMatch = createFinishedMatch(1L)
        val timeHistory = listOf(
            PlayerTimeHistory(playerId = 1L, matchId = 1L, elapsedTimeMillis = 60000L, savedAtMillis = 0L),
            PlayerTimeHistory(playerId = 1L, matchId = 2L, elapsedTimeMillis = 120000L, savedAtMillis = 0L),
        )
        val goals = listOf(
            Goal(id = 1L, matchId = 1L, scorerId = 1L, goalTimeMillis = 100L, matchElapsedTimeMillis = 100L, isOpponentGoal = false),
        )

        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player))
        every { matchRepository.getAllMatches() } returns flowOf(listOf(finishedMatch))
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(timeHistory)
        every { goalRepository.getAllTeamGoals() } returns flowOf(goals)

        val result = useCase.invoke().first()

        assertEquals(1, result.playerStats.size)
        val stats = result.playerStats[0]
        assertEquals(3.0, stats.totalTimeMinutes, 0.01) // (60000+120000)/60000
        assertEquals(2, stats.matchesPlayed)
        assertEquals(1, stats.goalsScored)
    }

    @Test
    fun `invoke should only include finished matches in matchResults`() = runTest {
        val finishedMatch = createFinishedMatch(1L)
        val scheduledMatch = Match(
            id = 2L, teamName = "Team A", opponent = "Opp", location = "Stadium",
            periodType = PeriodType.HALF_TIME, captainId = 1L, status = MatchStatus.SCHEDULED,
            dateTime = 2000L,
        )

        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { matchRepository.getAllMatches() } returns flowOf(listOf(finishedMatch, scheduledMatch))
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertEquals(1, result.matchResults.size) // Only FINISHED match
        assertEquals(1L, result.matchResults[0].match.id)
    }

    @Test
    fun `invoke should include only players with goals in topScorers`() = runTest {
        val player1 = createPlayer(1L)
        val player2 = createPlayer(2L)
        val goals = listOf(
            Goal(id = 1L, matchId = 1L, scorerId = 1L, goalTimeMillis = 100L, matchElapsedTimeMillis = 100L, isOpponentGoal = false),
        )

        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(goals)

        val result = useCase.invoke().first()

        // Only player1 scored goals, player2 should not be in topScorers
        assertEquals(1, result.topScorers.size)
        assertEquals(1L, result.topScorers[0].player.id)
        assertEquals(1, result.topScorers[0].totalGoals)
    }

    @Test
    fun `givenMultiplePlayersWithDifferentTotalTimes_whenInvoke_thenPlayerStatsSortedByTotalTimeDescending`() = runTest {
        // Given
        val player1 = createPlayer(1L)
        val player2 = createPlayer(2L)
        val timeHistory = listOf(
            PlayerTimeHistory(playerId = 1L, matchId = 1L, elapsedTimeMillis = 60000L, savedAtMillis = 0L),  // 1 min
            PlayerTimeHistory(playerId = 2L, matchId = 1L, elapsedTimeMillis = 120000L, savedAtMillis = 0L), // 2 min
        )

        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(timeHistory)
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        // When
        val result = useCase.invoke().first()

        // Then - player2 has more total time, should appear first
        assertEquals(2, result.playerStats.size)
        assertEquals(2L, result.playerStats[0].player.id)
        assertEquals(1L, result.playerStats[1].player.id)
    }

    @Test
    fun `givenMultipleFinishedMatchesWithDifferentDates_whenInvoke_thenMatchResultsSortedByDateAscending`() = runTest {
        // Given
        val match1 = createFinishedMatch(1L).copy(dateTime = 3000L)
        val match2 = createFinishedMatch(2L).copy(dateTime = 1000L)
        val match3 = createFinishedMatch(3L).copy(dateTime = 2000L)

        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { matchRepository.getAllMatches() } returns flowOf(listOf(match1, match2, match3))
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        // When
        val result = useCase.invoke().first()

        // Then - sorted by date ascending
        assertEquals(3, result.matchResults.size)
        assertEquals(1000L, result.matchResults[0].date)
        assertEquals(2000L, result.matchResults[1].date)
        assertEquals(3000L, result.matchResults[2].date)
    }

    @Test
    fun `givenMultipleScorersWithDifferentGoalCounts_whenInvoke_thenTopScorersSortedByGoalsDescending`() = runTest {
        // Given
        val player1 = createPlayer(1L)
        val player2 = createPlayer(2L)
        val goals = listOf(
            Goal(id = 1L, matchId = 1L, scorerId = 1L, goalTimeMillis = 100L, matchElapsedTimeMillis = 100L, isOpponentGoal = false),
            Goal(id = 2L, matchId = 1L, scorerId = 2L, goalTimeMillis = 200L, matchElapsedTimeMillis = 200L, isOpponentGoal = false),
            Goal(id = 3L, matchId = 2L, scorerId = 2L, goalTimeMillis = 300L, matchElapsedTimeMillis = 300L, isOpponentGoal = false),
        )

        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(goals)

        // When
        val result = useCase.invoke().first()

        // Then - player2 has 2 goals, should appear first
        assertEquals(2, result.topScorers.size)
        assertEquals(2L, result.topScorers[0].player.id)
        assertEquals(2, result.topScorers[0].totalGoals)
        assertEquals(1L, result.topScorers[1].player.id)
        assertEquals(1, result.topScorers[1].totalGoals)
    }

    @Test
    fun `givenFinishedMatchWithNullDateTime_whenInvoke_thenMatchExcludedFromMatchResults`() = runTest {
        // Given
        val matchWithDateTime = createFinishedMatch(1L)
        val matchWithoutDateTime = Match(
            id = 2L, teamName = "Team A", opponent = "Opp", location = "Stadium",
            periodType = PeriodType.HALF_TIME, captainId = 1L,
            status = MatchStatus.FINISHED, dateTime = null,
        )

        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { matchRepository.getAllMatches() } returns flowOf(listOf(matchWithDateTime, matchWithoutDateTime))
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        // When
        val result = useCase.invoke().first()

        // Then - only the match with a dateTime is included
        assertEquals(1, result.matchResults.size)
        assertEquals(1L, result.matchResults[0].match.id)
    }

    @Test
    fun `givenPlayerWithNoMatchHistory_whenInvoke_thenAverageTimeIsZeroAndMatchesPlayedIsZero`() = runTest {
        // Given
        val player = createPlayer(1L)

        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player))
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())
        every { goalRepository.getAllTeamGoals() } returns flowOf(emptyList())

        // When
        val result = useCase.invoke().first()

        // Then
        assertEquals(1, result.playerStats.size)
        assertEquals(0, result.playerStats[0].matchesPlayed)
        assertEquals(0.0, result.playerStats[0].averageTimePerMatch, 0.001)
        assertEquals(0.0, result.playerStats[0].totalTimeMinutes, 0.001)
    }

    private fun createPlayer(id: Long) = Player(
        id = id,
        firstName = "Player",
        lastName = "$id",
        number = id.toInt(),
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = false,
    )

    private fun createFinishedMatch(id: Long) = Match(
        id = id,
        teamName = "Team A",
        opponent = "Opponent",
        location = "Stadium",
        periodType = PeriodType.HALF_TIME,
        captainId = 1L,
        status = MatchStatus.FINISHED,
        dateTime = 1000L,
        goals = 2,
        opponentGoals = 1,
        periods = listOf(
            MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 1500000L),
        ),
    )
}
