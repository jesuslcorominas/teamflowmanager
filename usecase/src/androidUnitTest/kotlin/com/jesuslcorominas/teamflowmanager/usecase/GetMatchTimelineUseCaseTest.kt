package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetMatchTimelineUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var playerSubstitutionRepository: PlayerSubstitutionRepository
    private lateinit var playerRepository: PlayerRepository
    private lateinit var getMatchTimelineUseCase: GetMatchTimelineUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        goalRepository = mockk()
        playerSubstitutionRepository = mockk()
        playerRepository = mockk()
        getMatchTimelineUseCase = GetMatchTimelineUseCaseImpl(
            matchRepository = matchRepository,
            goalRepository = goalRepository,
            playerSubstitutionRepository = playerSubstitutionRepository,
            playerRepository = playerRepository,
        )
    }

    @Test
    fun `invoke should return null when match does not exist`() = runTest {
        // Given
        val matchId = 1L
        every { matchRepository.getMatchById(matchId, null) } returns flowOf(null)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke should return timeline with starting lineup event`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        assertEquals(1, result?.events?.size)
        val startingLineupEvent = result?.events?.first() as? TimelineEvent.StartingLineup
        assertEquals(0L, startingLineupEvent?.matchElapsedTimeMillis)
        assertEquals(2, startingLineupEvent?.players?.size)
    }

    @Test
    fun `invoke should return timeline with goal events and correct running score`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val match = createFinishedMatch(id = matchId)
        val goals = listOf(
            Goal(id = 1L, matchId = matchId, scorerId = 1L, goalTimeMillis = 300000L, matchElapsedTimeMillis = 300000L, isOpponentGoal = false),
            Goal(id = 2L, matchId = matchId, scorerId = null, goalTimeMillis = 600000L, matchElapsedTimeMillis = 600000L, isOpponentGoal = true),
            Goal(id = 3L, matchId = matchId, scorerId = 1L, goalTimeMillis = 900000L, matchElapsedTimeMillis = 900000L, isOpponentGoal = false),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(goals)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        val goalEvents = result?.events?.filterIsInstance<TimelineEvent.GoalScored>()
        assertEquals(3, goalEvents?.size)

        // Events are sorted ascending by time
        val firstGoal = goalEvents?.get(0)
        assertEquals(300000L, firstGoal?.matchElapsedTimeMillis)
        assertEquals(1, firstGoal?.teamScore)
        assertEquals(0, firstGoal?.opponentScore)
        assertEquals(false, firstGoal?.isOpponentGoal)

        val secondGoal = goalEvents?.get(1)
        assertEquals(600000L, secondGoal?.matchElapsedTimeMillis)
        assertEquals(1, secondGoal?.teamScore)
        assertEquals(1, secondGoal?.opponentScore)
        assertEquals(true, secondGoal?.isOpponentGoal)

        val lastGoal = goalEvents?.get(2)
        assertEquals(900000L, lastGoal?.matchElapsedTimeMillis)
        assertEquals(2, lastGoal?.teamScore) // After all 3 goals: 2 team goals
        assertEquals(1, lastGoal?.opponentScore) // 1 opponent goal
        assertEquals(false, lastGoal?.isOpponentGoal)
    }

    @Test
    fun `invoke should return timeline with substitution events`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val match = createFinishedMatch(id = matchId)
        val substitutions = listOf(
            PlayerSubstitution(
                id = 1L,
                matchId = matchId,
                playerOutId = 1L,
                playerInId = 2L,
                substitutionTimeMillis = 1500000L,
                matchElapsedTimeMillis = 1500000L,
            ),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(substitutions)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        val substitutionEvents = result?.events?.filterIsInstance<TimelineEvent.Substitution>()
        assertEquals(1, substitutionEvents?.size)
        val subEvent = substitutionEvents?.first()
        assertEquals(1500000L, subEvent?.matchElapsedTimeMillis)
        assertEquals(1L, subEvent?.playerOut?.id)
        assertEquals(2L, subEvent?.playerIn?.id)
    }

    @Test
    fun `invoke should return timeline with period break events`() = runTest {
        // Given
        val matchId = 1L
        val match = createFinishedMatch(
            id = matchId,
            periodType = PeriodType.HALF_TIME,
            periods = listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 1500000L,
                    startTimeMillis = 1000000L,
                    endTimeMillis = 2500000L,
                ),
                MatchPeriod(
                    periodNumber = 2,
                    periodDuration = 1500000L,
                    startTimeMillis = 2600000L,
                    endTimeMillis = 4100000L,
                ),
            ),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        val periodBreakEvents = result?.events?.filterIsInstance<TimelineEvent.PeriodBreak>()
        assertEquals(1, periodBreakEvents?.size) // One break after first half
        val breakEvent = periodBreakEvents?.first()
        assertEquals(1, breakEvent?.periodNumber)
        assertEquals(PeriodType.HALF_TIME, breakEvent?.periodType)
        // The break occurs after 1500000ms of play time (end - start of first period)
        assertEquals(1500000L, breakEvent?.matchElapsedTimeMillis)
    }

    @Test
    fun `invoke should return timeline events sorted by time ascending`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L),
        )
        val goals = listOf(
            Goal(id = 1L, matchId = matchId, scorerId = 1L, goalTimeMillis = 600000L, matchElapsedTimeMillis = 600000L, isOpponentGoal = false),
        )
        val substitutions = listOf(
            PlayerSubstitution(
                id = 1L,
                matchId = matchId,
                playerOutId = 1L,
                playerInId = 2L,
                substitutionTimeMillis = 900000L,
                matchElapsedTimeMillis = 900000L,
            ),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(goals)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(substitutions)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        assertEquals(3, result?.events?.size)
        // Events should be sorted by time ascending (first events at top)
        assertTrue(result?.events?.get(0) is TimelineEvent.StartingLineup) // 0L
        assertTrue(result?.events?.get(1) is TimelineEvent.GoalScored) // 600000L
        assertTrue(result?.events?.get(2) is TimelineEvent.Substitution) // 900000L
    }

    @Test
    fun `invoke should return score evolution with all goal points`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val match = createFinishedMatch(
            id = matchId,
            periods = listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 1500000L,
                    startTimeMillis = 0L,
                    endTimeMillis = 1500000L,
                ),
                MatchPeriod(
                    periodNumber = 2,
                    periodDuration = 1500000L,
                    startTimeMillis = 1600000L,
                    endTimeMillis = 3100000L,
                ),
            ),
        )
        val goals = listOf(
            Goal(id = 1L, matchId = matchId, scorerId = 1L, goalTimeMillis = 300000L, matchElapsedTimeMillis = 300000L, isOpponentGoal = false),
            Goal(id = 2L, matchId = matchId, scorerId = null, goalTimeMillis = 600000L, matchElapsedTimeMillis = 600000L, isOpponentGoal = true),
            Goal(id = 3L, matchId = matchId, scorerId = 1L, goalTimeMillis = 2000000L, matchElapsedTimeMillis = 2000000L, isOpponentGoal = false),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(goals)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        val scoreEvolution = result?.scoreEvolution
        // Should have: 2 start points (0-0), 3 goals, and 2 final points
        assertTrue(scoreEvolution!!.size >= 5)

        // Start points (impl adds two 0-0 points — one per team perspective)
        assertEquals(0L, scoreEvolution[0].timeMillis)
        assertEquals(0, scoreEvolution[0].teamScore)
        assertEquals(0, scoreEvolution[0].opponentScore)

        // First goal at index 2 (after the two 0-0 start points)
        assertEquals(300000L, scoreEvolution[2].timeMillis)
        assertEquals(1, scoreEvolution[2].teamScore)
        assertEquals(0, scoreEvolution[2].opponentScore)

        // Second goal at index 3
        assertEquals(600000L, scoreEvolution[3].timeMillis)
        assertEquals(1, scoreEvolution[3].teamScore)
        assertEquals(1, scoreEvolution[3].opponentScore)

        // Third goal at index 4
        assertEquals(2000000L, scoreEvolution[4].timeMillis)
        assertEquals(2, scoreEvolution[4].teamScore)
        assertEquals(1, scoreEvolution[4].opponentScore)
    }

    @Test
    fun `invoke should return score evolution starting at 0-0 even with no goals`() = runTest {
        // Given
        val matchId = 1L
        val match = createFinishedMatch(
            id = matchId,
            periods = listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 1500000L,
                    startTimeMillis = 0L,
                    endTimeMillis = 1500000L,
                ),
            ),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        val scoreEvolution = result?.scoreEvolution
        assertTrue(scoreEvolution!!.size >= 1)
        assertEquals(0L, scoreEvolution[0].timeMillis)
        assertEquals(0, scoreEvolution[0].teamScore)
        assertEquals(0, scoreEvolution[0].opponentScore)
    }

    private fun createPlayer(
        id: Long,
        firstName: String,
        lastName: String,
        number: Int,
    ) = Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = false,
    )

    private fun createFinishedMatch(
        id: Long,
        startingLineupIds: List<Long> = emptyList(),
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
        captainId = 1L,
        startingLineupIds = startingLineupIds,
        periods = periods,
    )

    @Test
    fun `invoke should propagate isOwnGoal from Goal to GoalScored timeline event`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val match = createFinishedMatch(id = matchId)
        val goals = listOf(
            Goal(id = 1L, matchId = matchId, scorerId = 1L, goalTimeMillis = 300000L, matchElapsedTimeMillis = 300000L, isOpponentGoal = false, isOwnGoal = false),
            Goal(id = 2L, matchId = matchId, scorerId = null, goalTimeMillis = 600000L, matchElapsedTimeMillis = 600000L, isOpponentGoal = false, isOwnGoal = true),
            Goal(id = 3L, matchId = matchId, scorerId = null, goalTimeMillis = 900000L, matchElapsedTimeMillis = 900000L, isOpponentGoal = true, isOwnGoal = false),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(goals)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        val goalEvents = result?.events?.filterIsInstance<TimelineEvent.GoalScored>()
        assertEquals(3, goalEvents?.size)

        val regularGoal = goalEvents?.find { it.matchElapsedTimeMillis == 300000L }
        assertEquals(false, regularGoal?.isOwnGoal)
        assertEquals(false, regularGoal?.isOpponentGoal)

        val ownGoal = goalEvents?.find { it.matchElapsedTimeMillis == 600000L }
        assertEquals(true, ownGoal?.isOwnGoal)
        assertEquals(false, ownGoal?.isOpponentGoal)

        val opponentGoal = goalEvents?.find { it.matchElapsedTimeMillis == 900000L }
        assertEquals(false, opponentGoal?.isOwnGoal)
        assertEquals(true, opponentGoal?.isOpponentGoal)
    }

    @Test
    fun `invoke should return player activity intervals for starting lineup`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
            periods = listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 1500000L,
                    startTimeMillis = 1000000L,
                    endTimeMillis = 2500000L,
                ),
            ),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(emptyList())
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        assertEquals(2, result?.playerActivity?.size)
        // Both players should have played from 0 to end of match
        val player1Activity = result?.playerActivity?.find { it.player.id == 1L }
        assertEquals(0L, player1Activity?.startTimeMillis)
        assertEquals(1500000L, player1Activity?.endTimeMillis)
    }

    @Test
    fun `invoke should return player activity intervals with substitutions`() = runTest {
        // Given
        val matchId = 1L
        val player1 = createPlayer(1L, "John", "Doe", 10)
        val player2 = createPlayer(2L, "Jane", "Smith", 7)
        val player3 = createPlayer(3L, "Mike", "Johnson", 9)
        val match = createFinishedMatch(
            id = matchId,
            startingLineupIds = listOf(1L, 2L),
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
                matchElapsedTimeMillis = 1500000L, // Halfway through match
            ),
        )

        every { matchRepository.getMatchById(matchId, null) } returns flowOf(match)
        every { goalRepository.getMatchGoals(matchId, null) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId, null) } returns flowOf(substitutions)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2, player3))

        // When
        val result = getMatchTimelineUseCase(matchId).first()

        // Then
        assertEquals(3, result?.playerActivity?.size)
        
        // Player 1 should have played from 0 to 1500000 (substitution time)
        val player1Activity = result?.playerActivity?.find { it.player.id == 1L }
        assertEquals(0L, player1Activity?.startTimeMillis)
        assertEquals(1500000L, player1Activity?.endTimeMillis)
        
        // Player 2 should have played full match
        val player2Activity = result?.playerActivity?.find { it.player.id == 2L }
        assertEquals(0L, player2Activity?.startTimeMillis)
        assertEquals(3000000L, player2Activity?.endTimeMillis)
        
        // Player 3 should have played from 1500000 to end
        val player3Activity = result?.playerActivity?.find { it.player.id == 3L }
        assertEquals(1500000L, player3Activity?.startTimeMillis)
        assertEquals(3000000L, player3Activity?.endTimeMillis)
    }
}
