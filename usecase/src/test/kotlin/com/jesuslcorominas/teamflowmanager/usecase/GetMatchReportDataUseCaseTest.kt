package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetMatchReportDataUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerRepository: PlayerRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var playerSubstitutionRepository: PlayerSubstitutionRepository
    private lateinit var getMatchReportDataUseCase: GetMatchReportDataUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        playerRepository = mockk()
        playerTimeHistoryRepository = mockk()
        goalRepository = mockk()
        playerSubstitutionRepository = mockk()
        getMatchReportDataUseCase = GetMatchReportDataUseCaseImpl(
            matchRepository = matchRepository,
            playerRepository = playerRepository,
            playerTimeHistoryRepository = playerTimeHistoryRepository,
            goalRepository = goalRepository,
            playerSubstitutionRepository = playerSubstitutionRepository,
        )
    }

    @Test
    fun `invoke should return null when match does not exist`() = runTest {
        // Given
        val matchId = 1L
        every { matchRepository.getMatchById(matchId) } returns flowOf(null)
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke should return match report data with player reports`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10, isGoalkeeper = false, isCaptain = true)
        val player2 = createPlayer(2L, "Jane", "Smith", 7, isGoalkeeper = true)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
            squadCallUpIds = listOf(1L, 2L),
            captainId = 1L,
        )
        val playerTimes = listOf(
            PlayerTimeHistory(playerId = 1L, matchId = matchId, elapsedTimeMillis = 3000000L, savedAtMillis = 0L),
            PlayerTimeHistory(playerId = 2L, matchId = matchId, elapsedTimeMillis = 3000000L, savedAtMillis = 0L),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(playerTimes)
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNotNull(result)
        assertEquals(2, result?.playerReports?.size)
        
        // Player reports should be sorted by number
        val firstReport = result?.playerReports?.get(0)
        assertEquals(7, firstReport?.number) // Jane Smith
        
        val secondReport = result?.playerReports?.get(1)
        assertEquals(10, secondReport?.number) // John Doe
        assertTrue(secondReport?.isCaptain == true)
        assertTrue(secondReport?.isStarter == true)
    }

    @Test
    fun `invoke should include timeline events in report data`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
            squadCallUpIds = listOf(1L, 2L),
        )
        val goals = listOf(
            Goal(id = 1L, matchId = matchId, scorerId = 1L, goalTimeMillis = 300000L, matchElapsedTimeMillis = 300000L, isOpponentGoal = false),
            Goal(id = 2L, matchId = matchId, scorerId = null, goalTimeMillis = 600000L, matchElapsedTimeMillis = 600000L, isOpponentGoal = true),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(goals)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNotNull(result)
        assertTrue(result!!.timelineEvents.isNotEmpty())
        
        // Should have starting lineup + 2 goals
        val startingLineupEvents = result.timelineEvents.filterIsInstance<TimelineEvent.StartingLineup>()
        assertEquals(1, startingLineupEvents.size)
        
        val goalEvents = result.timelineEvents.filterIsInstance<TimelineEvent.GoalScored>()
        assertEquals(2, goalEvents.size)
        
        // Verify goal running scores
        val firstGoal = goalEvents.find { it.matchElapsedTimeMillis == 300000L }
        assertEquals(1, firstGoal?.teamScore)
        assertEquals(0, firstGoal?.opponentScore)
        
        val secondGoal = goalEvents.find { it.matchElapsedTimeMillis == 600000L }
        assertEquals(1, secondGoal?.teamScore)
        assertEquals(1, secondGoal?.opponentScore)
    }

    @Test
    fun `invoke should include score evolution in report data`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val match = createFinishedMatch(
            id = matchId,
            squadCallUpIds = listOf(1L),
            periods = listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 1500000L,
                    startTimeMillis = 0L,
                    endTimeMillis = 1500000L,
                ),
            ),
        )
        val goals = listOf(
            Goal(id = 1L, matchId = matchId, scorerId = 1L, goalTimeMillis = 300000L, matchElapsedTimeMillis = 300000L, isOpponentGoal = false),
            Goal(id = 2L, matchId = matchId, scorerId = null, goalTimeMillis = 600000L, matchElapsedTimeMillis = 600000L, isOpponentGoal = true),
            Goal(id = 3L, matchId = matchId, scorerId = 1L, goalTimeMillis = 900000L, matchElapsedTimeMillis = 900000L, isOpponentGoal = false),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1))
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(goals)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNotNull(result)
        assertTrue(result!!.scoreEvolution.isNotEmpty())
        
        // Should have: start (0-0), 3 goals, and final point
        assertTrue(result.scoreEvolution.size >= 4)
        
        // Start point
        assertEquals(0L, result.scoreEvolution[0].timeMillis)
        assertEquals(0, result.scoreEvolution[0].teamScore)
        assertEquals(0, result.scoreEvolution[0].opponentScore)
        
        // First goal
        assertEquals(300000L, result.scoreEvolution[1].timeMillis)
        assertEquals(1, result.scoreEvolution[1].teamScore)
        assertEquals(0, result.scoreEvolution[1].opponentScore)
        
        // Second goal (opponent)
        assertEquals(600000L, result.scoreEvolution[2].timeMillis)
        assertEquals(1, result.scoreEvolution[2].teamScore)
        assertEquals(1, result.scoreEvolution[2].opponentScore)
        
        // Third goal
        assertEquals(900000L, result.scoreEvolution[3].timeMillis)
        assertEquals(2, result.scoreEvolution[3].teamScore)
        assertEquals(1, result.scoreEvolution[3].opponentScore)
    }

    @Test
    fun `invoke should include substitution events in timeline`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val player3 = createPlayer(3L, "Mike", "Johnson", 9)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
            squadCallUpIds = listOf(1L, 2L, 3L),
        )
        val substitutions = listOf(
            PlayerSubstitution(
                id = 1L,
                matchId = matchId,
                playerOutId = 1L,
                playerInId = 3L,
                substitutionTimeMillis = 2500000L,
                matchElapsedTimeMillis = 1500000L,
            ),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2, player3))
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNotNull(result)
        
        val substitutionEvents = result!!.timelineEvents.filterIsInstance<TimelineEvent.Substitution>()
        assertEquals(1, substitutionEvents.size)
        
        val subEvent = substitutionEvents.first()
        assertEquals(1500000L, subEvent.matchElapsedTimeMillis)
        assertEquals(1L, subEvent.playerOut.id)
        assertEquals(3L, subEvent.playerIn.id)
    }

    @Test
    fun `invoke should return empty timeline and score evolution when match has no events`() = runTest {
        // Given
        val matchId = 1L
        val match = createFinishedMatch(
            id = matchId,
            squadCallUpIds = emptyList(),
            startingLineupIds = emptyList(),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNotNull(result)
        assertTrue(result!!.timelineEvents.isEmpty())
        // Score evolution should still have starting point
        assertTrue(result.scoreEvolution.isNotEmpty())
        assertEquals(0, result.scoreEvolution[0].teamScore)
        assertEquals(0, result.scoreEvolution[0].opponentScore)
    }

    @Test
    fun `invoke should include player activity intervals`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val player3 = createPlayer(3L, "Mike", "Johnson", 9)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
            squadCallUpIds = listOf(1L, 2L, 3L),
            periods = listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 3000000L,
                    startTimeMillis = 1000000L,
                    endTimeMillis = 4000000L,
                ),
            ),
        )
        val substitutions = listOf(
            PlayerSubstitution(
                id = 1L,
                matchId = matchId,
                playerOutId = 1L,
                playerInId = 3L,
                substitutionTimeMillis = 2500000L,
                matchElapsedTimeMillis = 1500000L,
            ),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2, player3))
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { goalRepository.getMatchGoals(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)

        // When
        val result = getMatchReportDataUseCase(matchId).first()

        // Then
        assertNotNull(result)
        assertEquals(3, result!!.playerActivity.size)
        
        // Player 1 should have played from 0 to 1500000 (substitution time)
        val player1Activity = result.playerActivity.find { it.player.id == 1L }
        assertNotNull(player1Activity)
        assertEquals(0L, player1Activity!!.startTimeMillis)
        assertEquals(1500000L, player1Activity.endTimeMillis)
        
        // Player 2 should have played full match
        val player2Activity = result.playerActivity.find { it.player.id == 2L }
        assertNotNull(player2Activity)
        assertEquals(0L, player2Activity!!.startTimeMillis)
        assertEquals(3000000L, player2Activity.endTimeMillis)
        
        // Player 3 should have played from 1500000 to end
        val player3Activity = result.playerActivity.find { it.player.id == 3L }
        assertNotNull(player3Activity)
        assertEquals(1500000L, player3Activity!!.startTimeMillis)
        assertEquals(3000000L, player3Activity.endTimeMillis)
    }

    private fun createPlayer(
        id: Long,
        firstName: String,
        lastName: String,
        number: Int,
        isGoalkeeper: Boolean = false,
        isCaptain: Boolean = false,
    ) = Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = if (isGoalkeeper) listOf(Position.Goalkeeper) else listOf(Position.Forward),
        teamId = 1L,
        isCaptain = isCaptain,
    )

    private fun createFinishedMatch(
        id: Long,
        startingLineupIds: List<Long> = emptyList(),
        squadCallUpIds: List<Long> = emptyList(),
        captainId: Long = 1L,
        periodType: PeriodType = PeriodType.HALF_TIME,
        periods: List<MatchPeriod> = listOf(
            MatchPeriod(
                periodNumber = 1,
                periodDuration = 1500000L,
                startTimeMillis = 0L,
                endTimeMillis = 1500000L,
            ),
        ),
    ) = Match(
        id = id,
        teamName = "My Team",
        opponent = "Opponent",
        location = "Stadium",
        status = MatchStatus.FINISHED,
        periodType = periodType,
        captainId = captainId,
        startingLineupIds = startingLineupIds,
        squadCallUpIds = squadCallUpIds,
        periods = periods,
    )
}
