package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PauseMatchUseCaseTest {
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase

    @Before
    fun setup() {
        matchOperationRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        getMatchByIdUseCase = mockk(relaxed = true)
        pauseMatchUseCase = PauseMatchUseCaseImpl(
            matchOperationRepository = matchOperationRepository,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            playerTimeRepository = playerTimeRepository,
            getMatchByIdUseCase = getMatchByIdUseCase,
        )
        coEvery { matchOperationRepository.createOperation(any()) } returns "op1"
    }

    @Test
    fun `givenMatchNotFound_whenInvoke_thenDoNothing`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(null)

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test
    fun `givenMatchAlreadyPaused_whenInvoke_thenDoNothing`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.PAUSED, currentTime)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then — P2b: guard runs before createOperation so no orphaned operation is left
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test
    fun `givenMatchInProgressWithPlayingPlayers_whenInvoke_thenPauseAllPlayersAndMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            val playerTimes = listOf(
                PlayerTime(playerId = "1", isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING),
                PlayerTime(playerId = "2", isRunning = true, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PLAYING),
                PlayerTime(playerId = "3", isRunning = false, elapsedTimeMillis = 200L, status = PlayerTimeStatus.ON_BENCH),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify { playerTimeRepository.preparePauseTimersBatch(matchId, listOf("1", "2"), currentTime, "op1") }
            coVerify { matchOperationRepository.commitOperation(any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.PAUSED, commit.matchState!!.status)
            assertEquals("pauseCount must be incremented", match.pauseCount + 1, commit.matchState!!.pauseCount)

            // Periods: period 1 (the first not-yet-finished one) must carry endTimeMillis = currentTime.
            // Period 2 (startTimeMillis == 0) must remain untouched.
            val periods = commit.matchState!!.periods
            assertNotNull("commit matchState must carry periods", periods)
            assertEquals(
                "period 1 endTimeMillis must equal currentTime",
                currentTime,
                periods!![0].endTimeMillis,
            )
            assertEquals(
                "period 2 must remain unchanged (endTimeMillis == 0)",
                0L,
                periods[1].endTimeMillis,
            )
        }

    @Test
    fun `givenMatchInProgressWithNoPlayingPlayers_whenInvoke_thenOnlyPauseMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            val playerTimes = listOf(
                PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.ON_BENCH),
                PlayerTime(playerId = "2", isRunning = false, elapsedTimeMillis = 300L, status = PlayerTimeStatus.ON_BENCH),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatchWithOperationId(any(), any(), any(), any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.PAUSED, commit.matchState!!.status)
        }

    @Test
    fun `givenMatchInProgressWithNoPlayerTimes_whenInvoke_thenOnlyPauseMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(emptyList())

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatchWithOperationId(any(), any(), any(), any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
        }

    private fun createMatch(id: String, status: MatchStatus, currentTime: Long): Match {
        val startTime = currentTime - 500L
        return Match(
            id = id,
            teamId = "1",
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = "1",
            status = status,
            periods = listOf(
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = startTime, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
    }

    // -----------------------------------------------------------------------------------------
    // Regression test — #416: error handling
    // -----------------------------------------------------------------------------------------

    /**
     * #416 — Verifies that when the atomic write fails during match pause, the operation is marked
     * FAILED and lastCompletedOperationId is NOT advanced.
     */
    @Test
    fun `#416 when commitOperation throws during pause, operation is marked FAILED and lastCompletedOperationId is not advanced`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val operationId = "op1"
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            val playerTimes =
                listOf(PlayerTime(playerId = "1", isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            // The atomic write fails
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("Firestore write failed")

            // When — exception is expected to propagate
            var caughtException: Throwable? = null
            try {
                pauseMatchUseCase.invoke(matchId, currentTime)
            } catch (e: RuntimeException) {
                caughtException = e
            }

            // Then — operation is marked FAILED; lastCompletedOperationId is NOT advanced via
            // any separate repository call (it only moves inside the atomic batch, which failed).
            coVerify(exactly = 1) {
                matchOperationRepository.updateOperation(
                    match { it.id == operationId && it.status == MatchOperationStatus.FAILED },
                )
            }
            assertTrue("exception must propagate to caller", caughtException != null)
        }

    // -----------------------------------------------------------------------------------------
    // P1 — atomicity: if commitOperation throws, pauseCount is NOT incremented persistently
    // -----------------------------------------------------------------------------------------

    /**
     * P1 (atomicity) — when the batch commit fails during pause, pauseCount must NOT be
     * incremented in the backing store. Because [updateMatchState] is no longer called before
     * the commit, any failure in [commitOperation] leaves the match document untouched —
     * preventing the non-idempotent increment that feeds [Match.canPause].
     */
    /**
     * The use case no longer holds a MatchRepository, so it cannot write match state on its
     * own: that guarantee is structural and needs no test. What this does fix is the spec's
     * decision against automatic retries, which would duplicate a half-applied operation.
     */
    @Test
    fun `a failed commit during pause is not retried automatically`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            val playerTimes =
                listOf(PlayerTime(playerId = "1", isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("batch failed")

            // When
            try {
                pauseMatchUseCase.invoke(matchId, currentTime)
            } catch (_: RuntimeException) {
            }

            // Then — only commitOperation was attempted; it failed so no state is persisted
            coVerify(exactly = 1) { matchOperationRepository.commitOperation(any()) }
        }

    // -----------------------------------------------------------------------------------------
    // P1 — commit carries pauseCount incremented by exactly 1
    // -----------------------------------------------------------------------------------------

    @Test
    fun `P1 commit carries pauseCount incremented by 1 relative to current match pauseCount`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime).copy(pauseCount = 2)
            val playerTimes = listOf(
                PlayerTime(playerId = "1", isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then
            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals("pauseCount must be 3 (2 + 1)", 3, commit.matchState!!.pauseCount)
        }

    // -----------------------------------------------------------------------------------------
    // P2a — double-failure: updateOperation itself throws after commitOperation throws
    // -----------------------------------------------------------------------------------------

    @Test
    fun `P2a when both commitOperation and updateOperation throw, original exception propagates`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createMatch(matchId, MatchStatus.IN_PROGRESS, currentTime)
            val playerTimes =
                listOf(PlayerTime(playerId = "1", isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("commit failed")
            coEvery { matchOperationRepository.updateOperation(any()) } throws RuntimeException("update failed")

            // When — only the original exception propagates, secondary is suppressed
            var caughtException: Throwable? = null
            try {
                pauseMatchUseCase.invoke(matchId, currentTime)
            } catch (e: RuntimeException) {
                caughtException = e
            }

            // Then
            assertNotNull("original exception must propagate", caughtException)
            assertTrue("original exception message", caughtException!!.message == "commit failed")
        }

    // -----------------------------------------------------------------------------------------
    // P2b — guard before createOperation: no orphaned operation when all periods already finished
    // -----------------------------------------------------------------------------------------

    @Test
    fun `P2b when all periods already finished, no operation is created`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 3000L
            val match = Match(
                id = matchId,
                teamId = "1",
                teamName = "Team A",
                opponent = "Opponent",
                location = "Stadium",
                periodType = PeriodType.HALF_TIME,
                captainId = "1",
                status = MatchStatus.IN_PROGRESS,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 1501000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 2000L, endTimeMillis = 2501000L),
                ),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            pauseMatchUseCase.invoke(matchId, currentTime)

            // Then — no orphaned IN_PROGRESS operation is created
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
        }
}
