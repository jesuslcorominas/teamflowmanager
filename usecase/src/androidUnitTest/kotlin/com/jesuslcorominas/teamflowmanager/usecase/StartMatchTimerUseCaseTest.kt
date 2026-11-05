package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
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

class StartMatchTimerUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var startMatchTimerUseCase: StartMatchTimerUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        matchOperationRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        getMatchByIdUseCase = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        startMatchTimerUseCase = StartMatchTimerUseCaseImpl(
            matchRepository = matchRepository,
            matchOperationRepository = matchOperationRepository,
            playerTimeRepository = playerTimeRepository,
            getMatchByIdUseCase = getMatchByIdUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
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
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { matchRepository.updateMatchState(any(), any(), any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test
    fun `givenMatchWithStartingLineup_whenInvoke_thenStartMatchAndTimers`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val startingLineup = listOf("10", "11", "12")
            val match = createScheduledMatch(matchId, startingLineupIds = startingLineup)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify(exactly = 0) { matchRepository.updateMatchState(any(), any(), any(), any()) }
            coVerify { playerTimeRepository.prepareStartTimersBatch(matchId, startingLineup, currentTime, "op1") }
            coVerify { matchOperationRepository.commitOperation(any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.IN_PROGRESS, commit.matchState!!.status)
            assertNull("pauseCount must be null for start", commit.matchState!!.pauseCount)
        }

    @Test
    fun `givenMatchWithNoStartingLineup_whenInvoke_thenOnlyStartMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createScheduledMatch(matchId, startingLineupIds = emptyList())
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then — state travels in the commit, not via a separate updateMatchState call
            coVerify(exactly = 0) { matchRepository.updateMatchState(any(), any(), any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }

            val commit = commitSlot.captured
            assertNotNull("commit must carry matchState", commit.matchState)
            assertEquals(MatchStatus.IN_PROGRESS, commit.matchState!!.status)
        }

    @Test
    fun `givenAllPeriodsAlreadyStarted_whenInvoke_thenDoNotUpdateMatch`() =
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
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 2000L, endTimeMillis = 0L),
                ),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then — P2b: guard runs before createOperation so no orphaned operation is left
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { matchRepository.updateMatchState(any(), any(), any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
        }

    private fun createScheduledMatch(id: String, startingLineupIds: List<String> = emptyList()): Match {
        return Match(
            id = id,
            teamId = "1",
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = "1",
            status = MatchStatus.SCHEDULED,
            startingLineupIds = startingLineupIds,
            periods = listOf(
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
    }

    // -----------------------------------------------------------------------------------------
    // Regression test — #416: error handling
    // -----------------------------------------------------------------------------------------

    /**
     * #416 — Verifies that when the atomic write fails during match start, the operation is marked
     * FAILED and lastCompletedOperationId is NOT advanced.
     */
    @Test
    fun `#416 when commitOperation throws during start, operation is marked FAILED and lastCompletedOperationId is not advanced`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val operationId = "op1"
            val match = createScheduledMatch(matchId, startingLineupIds = listOf("player1"))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // The atomic write fails
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("Firestore write failed")

            // When — exception is expected to propagate
            var caughtException: Throwable? = null
            try {
                startMatchTimerUseCase.invoke(matchId, currentTime)
            } catch (e: RuntimeException) {
                caughtException = e
            }

            // Then — operation is marked FAILED; lastCompletedOperationId is NOT advanced
            coVerify(exactly = 1) {
                matchOperationRepository.updateOperation(
                    match { it.id == operationId && it.status == MatchOperationStatus.FAILED },
                )
            }
            coVerify(exactly = 0) { matchRepository.updateLastCompletedOperationId(any(), any()) }
            @Suppress("DEPRECATION")
            coVerify(exactly = 0) { matchRepository.updateMatchWithOperationId(any(), any()) }
            assertTrue("exception must propagate to caller", caughtException != null)
        }

    // -----------------------------------------------------------------------------------------
    // P1 — atomicity: if commitOperation throws, no match state is persisted
    // -----------------------------------------------------------------------------------------

    /**
     * P1 (atomicity) — when the batch commit fails during match start, the match state
     * (status and periods) must NOT be persisted independently. Because [updateMatchState] is no
     * longer called before the commit, any failure in [commitOperation] leaves the match
     * document untouched.
     */
    @Test
    fun `P1 when commitOperation throws during start, match state is not persisted independently`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 1000L
            val match = createScheduledMatch(matchId, startingLineupIds = listOf("player1"))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("batch failed")

            // When
            try {
                startMatchTimerUseCase.invoke(matchId, currentTime)
            } catch (_: RuntimeException) {
            }

            // Then — no separate updateMatchState call was made; state only travels in the batch
            coVerify(exactly = 0) { matchRepository.updateMatchState(any(), any(), any(), any()) }
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
            val match = createScheduledMatch(matchId, startingLineupIds = listOf("player1"))
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("commit failed")
            coEvery { matchOperationRepository.updateOperation(any()) } throws RuntimeException("update failed")

            // When — only the original exception propagates, secondary is suppressed
            var caughtException: Throwable? = null
            try {
                startMatchTimerUseCase.invoke(matchId, currentTime)
            } catch (e: RuntimeException) {
                caughtException = e
            }

            // Then
            assertNotNull("original exception must propagate", caughtException)
            assertTrue("original exception message", caughtException!!.message == "commit failed")
        }

    // -----------------------------------------------------------------------------------------
    // P1 — createdAt: operation is stamped with the caller's currentTimeMillis
    // -----------------------------------------------------------------------------------------

    @Test
    fun `P1 operation createdAt equals currentTimeMillis passed to invoke`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTime = 42000L
            val match = createScheduledMatch(matchId)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then — the MatchOperation created has createdAt == currentTime
            coVerify {
                matchOperationRepository.createOperation(
                    match { it.createdAt == currentTime },
                )
            }
        }
}
