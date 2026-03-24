package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FinishMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var finishMatchUseCase: FinishMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        matchOperationRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        playerTimeHistoryRepository = mockk(relaxed = true)
        finishMatchUseCase = FinishMatchUseCaseImpl(
            matchRepository = matchRepository,
            matchOperationRepository = matchOperationRepository,
            playerTimeRepository = playerTimeRepository,
            playerTimeHistoryRepository = playerTimeHistoryRepository,
        )
        coEvery { matchOperationRepository.createOperation(any()) } returns "op1"
    }

    @Test
    fun `givenMatchNotFound_whenInvoke_thenDoNothing`() =
        runTest {
            // Given
            val matchId = 1L
            every { matchRepository.getMatchById(matchId) } returns flowOf(null)

            // When
            finishMatchUseCase.invoke(matchId, System.currentTimeMillis())

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `givenMatchNotInCorrectStatus_whenInvoke_thenDoNothing`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = System.currentTimeMillis()
            val match = createMatch(matchId, MatchStatus.SCHEDULED, currentTime)
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `givenMatchInProgress_whenInvoke_thenFinishMatchAndSaveHistoryAndReset`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = System.currentTimeMillis()
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            val playerTimes = listOf(
                PlayerTime(playerId = 1L, elapsedTimeMillis = 3000L, isRunning = false),
                PlayerTime(playerId = 2L, elapsedTimeMillis = 2000L, isRunning = false),
            )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.FINISHED }
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match { it.playerId == 1L && it.matchId == matchId && it.elapsedTimeMillis == 3000L }
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match { it.playerId == 2L && it.matchId == matchId && it.elapsedTimeMillis == 2000L }
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `givenMatchPaused_whenInvoke_thenFinishMatch`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = System.currentTimeMillis()
            val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.FINISHED }
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `givenPlayerWithZeroElapsedTime_whenInvoke_thenSkipHistory`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = System.currentTimeMillis()
            val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
            val playerTimes = listOf(
                PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = false),
                PlayerTime(playerId = 2L, elapsedTimeMillis = 2000L, isRunning = false),
            )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then - player 1 (zero time) should NOT be saved
            coVerify(exactly = 0) {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match { it.playerId == 1L }
                )
            }
            // Player 2 (2000ms) should be saved
            coVerify(exactly = 1) {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match { it.playerId == 2L }
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `givenRunningPlayerTime_whenInvoke_thenCalculateFinalElapsedTime`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = System.currentTimeMillis()
            val lastStart = currentTime - 1000L
            val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
            val playerTimes = listOf(
                PlayerTime(
                    playerId = 1L,
                    elapsedTimeMillis = 3000L,
                    isRunning = true,
                    lastStartTimeMillis = lastStart,
                ),
            )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then - elapsed should be > 3000L (accumulated + running time)
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match { it.playerId == 1L && it.elapsedTimeMillis > 3000L }
                )
            }
        }

    @Test
    fun `givenNoPlayerTimes_whenInvoke_thenSaveNoHistory`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = System.currentTimeMillis()
            val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())

            // When
            finishMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `givenMatchInTimeoutStatus_whenInvoke_thenDoNothing`() = runTest {
        // Given
        val matchId = 1L
        val currentTime = System.currentTimeMillis()
        val match = createMatch(matchId, MatchStatus.TIMEOUT, currentTime)
        every { matchRepository.getMatchById(matchId) } returns flowOf(match)

        // When
        finishMatchUseCase.invoke(matchId, currentTime)

        // Then
        coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
        coVerify(exactly = 0) { playerTimeRepository.resetAllPlayerTimes() }
    }

    @Test
    fun `givenMatchAlreadyFinished_whenInvoke_thenDoNothing`() = runTest {
        // Given
        val matchId = 1L
        val currentTime = System.currentTimeMillis()
        val match = createMatch(matchId, MatchStatus.FINISHED, currentTime)
        every { matchRepository.getMatchById(matchId) } returns flowOf(match)

        // When
        finishMatchUseCase.invoke(matchId, currentTime)

        // Then
        coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
        coVerify(exactly = 0) { playerTimeRepository.resetAllPlayerTimes() }
    }

    @Test
    fun `givenMatchInProgressWithNoStartedPeriod_whenInvoke_thenFinishMatchWithoutClosingActivePeriod`() = runTest {
        // Given
        val matchId = 1L
        val currentTime = System.currentTimeMillis()
        val match = Match(
            id = matchId,
            teamId = 1L,
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            status = MatchStatus.IN_PROGRESS,
            periods = listOf(
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())

        // When
        finishMatchUseCase.invoke(matchId, currentTime)

        // Then - match finishes even without any started period
        coVerify { matchRepository.updateMatch(match { it.status == MatchStatus.FINISHED }) }
        coVerify { playerTimeRepository.resetAllPlayerTimes() }
    }

    @Test
    fun `givenRunningPlayerWithNullLastStartTime_whenInvoke_thenUseAccumulatedElapsedTime`() = runTest {
        // Given
        val matchId = 1L
        val currentTime = System.currentTimeMillis()
        val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
        val playerTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 5000L, isRunning = true, lastStartTimeMillis = null),
        )
        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(playerTimes)
        coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L

        // When
        finishMatchUseCase.invoke(matchId, currentTime)

        // Then - isRunning=true but lastStartTimeMillis=null → condition is false, uses elapsedTimeMillis as-is
        coVerify {
            playerTimeHistoryRepository.insertPlayerTimeHistory(
                match { it.playerId == 1L && it.elapsedTimeMillis == 5000L }
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `givenResetPlayerTimesThrows_whenInvoke_thenThrowIllegalStateException`() = runTest {
        // Given
        val matchId = 1L
        val currentTime = System.currentTimeMillis()
        val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())
        coEvery { playerTimeRepository.resetAllPlayerTimes() } throws RuntimeException("DB error")

        // When - expects IllegalStateException wrapping the original exception
        finishMatchUseCase.invoke(matchId, currentTime)
    }

    private fun createMatch(id: Long, status: MatchStatus, currentTime: Long): Match {
        val startTime = currentTime - 1500000L
        return Match(
            id = id,
            teamId = 1L,
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            status = status,
            periods = listOf(
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = startTime, endTimeMillis = currentTime),
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
    }
}
