package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
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
    private lateinit var transactionRunner: TransactionRunner
    private lateinit var registerGoalUseCase: RegisterGoalUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        goalRepository = mockk(relaxed = true)
        transactionRunner = mockk()
        coEvery { transactionRunner.run<Long>(any()) } coAnswers {
            val block = firstArg<suspend () -> Long>()
            block()
        }
        registerGoalUseCase =
            RegisterGoalUseCaseImpl(
                matchRepository,
                goalRepository,
                transactionRunner,
            )
    }

    @Test
    fun `invoke should record goal with correct match elapsed time when match is running`() =
        runTest {
            // Given
            val matchId = 1L
            val scorerId = 2L
            val currentTimeMillis = System.currentTimeMillis()
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 900000L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis - 60000L,
                    teamName = "Team B"
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
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 600000L,
                    isRunning = false,
                    lastStartTimeMillis = null,
                    teamName = "Team B"
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
            val lastStartTimeMillis = currentTimeMillis - 120000L
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 300000L,
                    isRunning = true,
                    lastStartTimeMillis = lastStartTimeMillis,
                    teamName = "Team B"
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
            val scorerId = null // Null scorer ID for opponent goals
            val currentTimeMillis = System.currentTimeMillis()
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 500000L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis - 30000L,
                    teamName = "Team B"
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
            val scorerId = null // Null scorer ID for own goals (scored by rival in their own net)
            val currentTimeMillis = System.currentTimeMillis()
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 700000L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis - 45000L,
                    teamName = "Team B"
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
                isOwnGoal = true
            )

            // Then
            coVerify { goalRepository.insertGoal(any()) }

            val goal = goalSlot.captured
            assertEquals(matchId, goal.matchId)
            assertEquals(null, goal.scorerId) // No scorer - it's an own goal by rival
            assertEquals(currentTimeMillis, goal.goalTimeMillis)
            assertEquals(745000L, goal.matchElapsedTimeMillis) // 700000 + 45000
            assertEquals(false, goal.isOpponentGoal) // Counts for OUR team
            assertEquals(true, goal.isOwnGoal)
            assertEquals(1L, result)
        }
}
