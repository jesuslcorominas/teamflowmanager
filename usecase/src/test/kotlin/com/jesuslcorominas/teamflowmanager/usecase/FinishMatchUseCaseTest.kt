package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FinishMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var transactionRunner: TransactionRunner
    private lateinit var finishMatchUseCase: FinishMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        playerTimeHistoryRepository = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        
        // Make transactionRunner execute blocks immediately
        coEvery { transactionRunner.run<Unit>(any()) } answers {
            val block = firstArg<suspend () -> Unit>()
            block.invoke()
        }
        
        finishMatchUseCase = FinishMatchUseCaseImpl(
            matchRepository = matchRepository,
            playerTimeRepository = playerTimeRepository,
            playerTimeHistoryRepository = playerTimeHistoryRepository,
            transactionRunner = transactionRunner
        )
    }

    @Test
    fun `invoke should do nothing when no match exists`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            every { matchRepository.getMatchById(matchId) } returns flowOf(null)

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.getAllPlayerTimes() }
            coVerify(exactly = 0) { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify(exactly = 0) { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should do nothing when match is already finished`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 5000L
            val match = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.FINISHED
            )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
        }

    @Test
    fun `invoke should finish match and close current period when IN_PROGRESS`() =
        runTest {
            // Given
            val matchId = 1L
            val startTime = 1000L
            val currentTime = 5000L
            val match = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.IN_PROGRESS,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = PeriodType.HALF_TIME.duration, startTimeMillis = startTime),
                    MatchPeriod(periodNumber = 2, periodDuration = PeriodType.HALF_TIME.duration)
                )
            )
            val playerTimes = listOf(
                PlayerTime(playerId = 1L, elapsedTimeMillis = 3000L, isRunning = false),
                PlayerTime(playerId = 2L, elapsedTimeMillis = 2000L, isRunning = true, lastStartTimeMillis = 2000L),
            )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } just runs
            coEvery { matchRepository.updateMatch(any()) } just runs

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            val matchSlot = slot<Match>()
            coVerify { matchRepository.updateMatch(capture(matchSlot)) }
            
            assertEquals(MatchStatus.FINISHED, matchSlot.captured.status)
            assertEquals(currentTime, matchSlot.captured.periods[0].endTimeMillis)
            
            coVerify(exactly = 2) { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should finish match when PAUSED without closing period`() =
        runTest {
            // Given
            val matchId = 1L
            val startTime = 1000L
            val pauseTime = 3000L
            val currentTime = 5000L
            val match = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.PAUSED,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = PeriodType.HALF_TIME.duration, startTimeMillis = startTime, endTimeMillis = pauseTime),
                    MatchPeriod(periodNumber = 2, periodDuration = PeriodType.HALF_TIME.duration)
                )
            )
            val playerTimes = listOf(
                PlayerTime(playerId = 1L, elapsedTimeMillis = 2000L, isRunning = false)
            )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } just runs
            coEvery { matchRepository.updateMatch(any()) } just runs

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            val matchSlot = slot<Match>()
            coVerify { matchRepository.updateMatch(capture(matchSlot)) }
            
            assertEquals(MatchStatus.FINISHED, matchSlot.captured.status)
            assertEquals(pauseTime, matchSlot.captured.periods[0].endTimeMillis) // Should not change
            
            coVerify { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }
}
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 1L &&
                            it.matchId == matchId &&
                            it.elapsedTimeMillis == 3000L
                    },
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 2L &&
                            it.matchId == matchId &&
                            it.elapsedTimeMillis == 2000L
                    },
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should calculate final elapsed time for running players`() =
        runTest {
            // Given
            val matchId = 1L
            val startTime = 1000L
            val match = Match(
                id = matchId,
                elapsedTimeMillis = 5000L,
                isRunning = true,
                lastStartTimeMillis = startTime,
                teamName = "Team A"
            , location = "Test Location", opponent = "Test Opponent", periodType = PeriodType.HALF_TIME, captainId = 1L)
            val playerTimes =
                listOf(
                    PlayerTime(
                        playerId = 1L,
                        elapsedTimeMillis = 3000L,
                        isRunning = true,
                        lastStartTimeMillis = startTime,
                    ),
                )
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match {
                        it.id == matchId &&
                            it.isRunning == false &&
                            it.elapsedTimeMillis > 5000L && // Should be 5000L + current time - startTime
                            it.lastStartTimeMillis == null &&
                            it.status == MatchStatus.FINISHED
                    },
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 1L &&
                            it.matchId == matchId &&
                            it.elapsedTimeMillis > 3000L // Should be 3000L + current time - startTime
                    },
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should not save player times with zero elapsed time`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(id = matchId, elapsedTimeMillis = 5000L, isRunning = false, teamName = "Team A", location = "Test Location", opponent = "Test Opponent", periodType = PeriodType.HALF_TIME, captainId = 1L)
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = false),
                    PlayerTime(playerId = 2L, elapsedTimeMillis = 2000L, isRunning = false),
                )
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify(exactly = 0) {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 1L
                    },
                )
            }
            coVerify(exactly = 1) {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 2L
                    },
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should save empty list when no player times exist`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(id = matchId, elapsedTimeMillis = 5000L, isRunning = false, teamName = "Team A", location = "Test Location", opponent = "Test Opponent", periodType = PeriodType.HALF_TIME, captainId = 1L)
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(emptyList())
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify(exactly = 0) { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }
}
