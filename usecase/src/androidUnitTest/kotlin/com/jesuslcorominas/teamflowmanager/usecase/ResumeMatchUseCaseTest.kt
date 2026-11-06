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
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ResumeMatchUseCaseTest {
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase

    @Before
    fun setup() {
        matchOperationRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        getMatchByIdUseCase = mockk(relaxed = true)
        resumeMatchUseCase = ResumeMatchUseCaseImpl(
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
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test
    fun `givenMatchPausedWithPausedPlayers_whenInvoke_thenResumeAllPausedPlayersAndMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createPausedMatch(matchId)
            val playerTimes = listOf(
                PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED),
                PlayerTime(playerId = "2", isRunning = false, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PAUSED),
                PlayerTime(playerId = "3", isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify { playerTimeRepository.prepareStartTimersBatch(matchId, listOf("1", "2"), currentTime, "op1") }
            coVerify { matchOperationRepository.commitOperation(any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.IN_PROGRESS, commit.matchState!!.status)
            assertNull("pauseCount must be null for resume", commit.matchState!!.pauseCount)

            // Periods: period 2 (the first not-yet-started one) must carry startTimeMillis = currentTime.
            // Period 1 (already finished) must remain untouched.
            val periods = commit.matchState!!.periods
            assertNotNull("commit matchState must carry periods", periods)
            assertEquals(
                "period 1 must remain unchanged (startTimeMillis = 1000L)",
                1000L,
                periods!![0].startTimeMillis,
            )
            assertEquals(
                "period 2 startTimeMillis must equal currentTime",
                currentTime,
                periods[1].startTimeMillis,
            )
        }

    @Test
    fun `givenMatchPausedWithNoPausedPlayers_whenInvoke_thenOnlyResumeMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createPausedMatch(matchId)
            val playerTimes = listOf(
                PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
                PlayerTime(playerId = "2", isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.IN_PROGRESS, commit.matchState!!.status)
        }

    @Test
    fun `givenMatchPausedWithNoPlayerTimes_whenInvoke_thenOnlyResumeMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createPausedMatch(matchId)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(emptyList())

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
        }

    private fun createPausedMatch(id: String): Match {
        return Match(
            id = id,
            teamId = "1",
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = "1",
            status = MatchStatus.PAUSED,
            periods = listOf(
                // First period finished
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 1501000L),
                // Second period not started yet
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
    }

    // -----------------------------------------------------------------------------------------
    // Regression tests — #423: atomic commit, #416: error handling
    // -----------------------------------------------------------------------------------------

    /**
     * #423 — Verifies that resuming a match with paused players is persisted through a SINGLE
     * commitOperation call that includes all resumed PlayerTimes and lastCompletedOperationId.
     */
    @Test
    fun `#423 when match has paused players, single commitOperation call includes their PlayerTimes and lastCompletedOperationId`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val operationId = "op1"
            val match = createPausedMatch(matchId)
            val pausedPlayerTime =
                PlayerTime(playerId = "1", isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING, lastOperationId = operationId)
            val pausedPlayers =
                listOf(
                    PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED),
                    PlayerTime(playerId = "2", isRunning = false, elapsedTimeMillis = 300L, status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(pausedPlayers)
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("1"), currentTime, operationId)
            } returns listOf(pausedPlayerTime)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then — single atomic write, no separate writes
            coVerify(exactly = 1) { matchOperationRepository.commitOperation(any()) }

            // Verify commit content
            val commit = commitSlot.captured
            assertTrue("commit must include the resumed player's time", commit.playerTimes.any { it.playerId == "1" })
            assertEquals("lastCompletedOperationId must match the created operation id", operationId, commit.lastCompletedOperationId)
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.IN_PROGRESS, commit.matchState!!.status)
        }

    /**
     * #416 — Verifies that when the atomic write fails during resume, the operation is marked
     * FAILED and lastCompletedOperationId is NOT advanced.
     */
    @Test
    fun `#416 when commitOperation throws during resume, operation is marked FAILED and lastCompletedOperationId is not advanced`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val operationId = "op1"
            val match = createPausedMatch(matchId)
            val pausedPlayers =
                listOf(PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(pausedPlayers)
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("1"), currentTime, operationId)
            } returns listOf(PlayerTime(playerId = "1", isRunning = true, status = PlayerTimeStatus.PLAYING))

            // The atomic write fails
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("Firestore write failed")

            // When — exception is expected to propagate
            var caughtException: Throwable? = null
            try {
                resumeMatchUseCase.invoke(matchId, currentTime)
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
    // P1 — atomicity: if commitOperation throws, no match state is persisted
    // -----------------------------------------------------------------------------------------

    /**
     * P1 (atomicity) — when the batch commit fails during resume, the match state (status and
     * periods) must NOT be persisted independently. Because [updateMatchState] is no longer
     * called before the commit, any failure in [commitOperation] leaves the match document
     * untouched.
     */
    /**
     * The use case no longer holds a MatchRepository, so it cannot write match state on its
     * own: that guarantee is structural and needs no test. What this does fix is the spec's
     * decision against automatic retries, which would duplicate a half-applied operation.
     */
    @Test
    fun `a failed commit during resume is not retried automatically`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val operationId = "op1"
            val match = createPausedMatch(matchId)
            val pausedPlayers =
                listOf(PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(pausedPlayers)
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("1"), currentTime, operationId)
            } returns listOf(PlayerTime(playerId = "1", isRunning = true, status = PlayerTimeStatus.PLAYING))
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("batch failed")

            // When
            try {
                resumeMatchUseCase.invoke(matchId, currentTime)
            } catch (_: RuntimeException) {
            }

            // Then — only commitOperation was attempted; it failed so no state is persisted
            coVerify(exactly = 1) { matchOperationRepository.commitOperation(any()) }
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
            val operationId = "op1"
            val match = createPausedMatch(matchId)
            val pausedPlayers =
                listOf(PlayerTime(playerId = "1", isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(pausedPlayers)
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("1"), currentTime, operationId)
            } returns listOf(PlayerTime(playerId = "1", isRunning = true, status = PlayerTimeStatus.PLAYING))
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("commit failed")
            coEvery { matchOperationRepository.updateOperation(any()) } throws RuntimeException("update failed")

            // When — only the original exception propagates, secondary is suppressed
            var caughtException: Throwable? = null
            try {
                resumeMatchUseCase.invoke(matchId, currentTime)
            } catch (e: RuntimeException) {
                caughtException = e
            }

            // Then
            assertNotNull("original exception must propagate", caughtException)
            assertTrue("original exception message", caughtException!!.message == "commit failed")
        }

    // -----------------------------------------------------------------------------------------
    // P2b — guard before createOperation: no orphaned operation when no period left to start
    // -----------------------------------------------------------------------------------------

    @Test
    fun `P2b when all periods already started, no operation is created`() =
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
                status = MatchStatus.PAUSED,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 1501000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 2000L, endTimeMillis = 0L),
                ),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then — no orphaned IN_PROGRESS operation is created
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
        }
}
