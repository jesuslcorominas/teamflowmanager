package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RegisterGoalUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var registerGoalUseCase: RegisterGoalUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        goalRepository = mockk(relaxed = true)
        registerGoalUseCase =
            RegisterGoalUseCaseImpl(
                matchRepository,
                goalRepository,
            )
    }

    @Test
    fun `invoke should record goal with correct match elapsed time when match is running`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = 2L
            val currentTimeMillis = System.currentTimeMillis()
            // Period 1 finished: 900000ms played. Period 2 running: started 60000ms ago.
            // Total elapsed = 900000 + 60000 = 960000ms
            val match = Match(
                id = matchId,
                teamId = 1L,
                teamName = "Team B",
                opponent = "Opponent",
                location = "Stadium",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 901000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = currentTimeMillis - 60000L, endTimeMillis = 0L),
                ),
            )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val goalSlot = slot<Goal>()
            coEvery { goalRepository.insertGoal(capture(goalSlot)) } returns 1L

            // When
            val result = registerGoalUseCase(matchId, scorerId, currentTimeMillis)

            // Then
            coVerify { goalRepository.insertGoal(any()) }

            val goal = goalSlot.captured
            assertEquals(matchId, goal.matchId)
            assertEquals(scorerId, goal.scorerId)
            assertEquals(currentTimeMillis, goal.goalTimeMillis)
            assertEquals(960000L, goal.matchElapsedTimeMillis) // 900000 + 60000
            assertEquals(1L, result)
        }

    @Test
    fun `invoke should use elapsed time when match is paused`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = 2L
            val currentTimeMillis = System.currentTimeMillis()
            // One finished period: 600000ms played
            val match = Match(
                id = matchId,
                teamId = 1L,
                teamName = "Team B",
                opponent = "Opponent",
                location = "Stadium",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 601000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
                ),
            )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val goalSlot = slot<Goal>()
            coEvery { goalRepository.insertGoal(capture(goalSlot)) } returns 1L

            // When
            registerGoalUseCase(matchId, scorerId, currentTimeMillis)

            // Then
            val goal = goalSlot.captured
            assertEquals(600000L, goal.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should calculate correct match elapsed time with different values`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = 2L
            val currentTimeMillis = System.currentTimeMillis()
            // Period 1 finished: 300000ms. Period 2 running: 120000ms ago.
            // Total = 300000 + 120000 = 420000ms
            val match = Match(
                id = matchId,
                teamId = 1L,
                teamName = "Team B",
                opponent = "Opponent",
                location = "Stadium",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 301000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = currentTimeMillis - 120000L, endTimeMillis = 0L),
                ),
            )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val goalSlot = slot<Goal>()
            coEvery { goalRepository.insertGoal(capture(goalSlot)) } returns 1L

            // When
            registerGoalUseCase(matchId, scorerId, currentTimeMillis)

            // Then
            val goal = goalSlot.captured
            assertEquals(420000L, goal.matchElapsedTimeMillis) // 300000 + 120000
        }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke should throw exception when no active match found`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = 2L
            val currentTimeMillis = System.currentTimeMillis()
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(null)

            // When
            registerGoalUseCase(matchId, scorerId, currentTimeMillis)

            // Then - should throw exception
        }

    @Test
    fun `invoke should record opponent goal correctly`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = null
            val currentTimeMillis = System.currentTimeMillis()
            // Period 1 finished: 500000ms. Period 2 running: 30000ms ago.
            // Total = 500000 + 30000 = 530000ms
            val match = Match(
                id = matchId,
                teamId = 1L,
                teamName = "Team B",
                opponent = "Opponent",
                location = "Stadium",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 501000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = currentTimeMillis - 30000L, endTimeMillis = 0L),
                ),
            )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val goalSlot = slot<Goal>()
            coEvery { goalRepository.insertGoal(capture(goalSlot)) } returns 1L

            // When
            val result = registerGoalUseCase(matchId, scorerId, currentTimeMillis, isOpponentGoal = true)

            // Then
            coVerify { goalRepository.insertGoal(any()) }

            val goal = goalSlot.captured
            assertEquals(matchId, goal.matchId)
            assertEquals(scorerId, goal.scorerId)
            assertEquals(currentTimeMillis, goal.goalTimeMillis)
            assertEquals(530000L, goal.matchElapsedTimeMillis) // 500000 + 30000
            assertEquals(true, goal.isOpponentGoal)
            assertEquals(1L, result)
        }

    @Test
    fun `invoke should record own goal correctly without scorer id`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = null
            val currentTimeMillis = System.currentTimeMillis()
            // Period 1 finished: 700000ms. Period 2 running: 45000ms ago.
            // Total = 700000 + 45000 = 745000ms
            val match = Match(
                id = matchId,
                teamId = 1L,
                teamName = "Team B",
                opponent = "Opponent",
                location = "Stadium",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 701000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = currentTimeMillis - 45000L, endTimeMillis = 0L),
                ),
            )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val goalSlot = slot<Goal>()
            coEvery { goalRepository.insertGoal(capture(goalSlot)) } returns 1L

            // When
            val result = registerGoalUseCase(
                matchId = matchId,
                scorerId = scorerId,
                currentTimeMillis = currentTimeMillis,
                isOpponentGoal = false,
                isOwnGoal = true,
            )

            // Then
            coVerify { goalRepository.insertGoal(any()) }

            val goal = goalSlot.captured
            assertEquals(matchId, goal.matchId)
            assertEquals(null, goal.scorerId)
            assertEquals(currentTimeMillis, goal.goalTimeMillis)
            assertEquals(745000L, goal.matchElapsedTimeMillis) // 700000 + 45000
            assertEquals(false, goal.isOpponentGoal)
            assertEquals(true, goal.isOwnGoal)
            assertEquals(1L, result)
        }
}
