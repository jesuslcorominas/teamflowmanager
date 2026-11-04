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
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ResumeMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        matchOperationRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        getMatchByIdUseCase = mockk(relaxed = true)
        resumeMatchUseCase = ResumeMatchUseCaseImpl(
            matchRepository = matchRepository,
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
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
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

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { playerTimeRepository.prepareStartTimersBatch(matchId, listOf("1", "2"), currentTime, "op1") }
            coVerify { matchOperationRepository.commitOperation(any()) }
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.IN_PROGRESS }
                )
            }
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

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.IN_PROGRESS }
                )
            }
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

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.IN_PROGRESS }
                )
            }
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
     *
     * Regression: currently the impl makes separate calls to startTimersBatchWithOperationId,
     * updateMatch, updateOperation and updateMatchWithOperationId — never commitOperation.
     * This test fails until T004 migrates the use case to the atomic pattern.
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
            @Suppress("DEPRECATION")
            coVerify(exactly = 0) { matchRepository.updateMatchWithOperationId(any(), any()) }
            coVerify(exactly = 0) { matchRepository.updateLastCompletedOperationId(any(), any()) }

            // Verify commit content
            val commit = commitSlot.captured
            assertTrue("commit must include the resumed player's time", commit.playerTimes.any { it.playerId == "1" })
            assertEquals("lastCompletedOperationId must match the created operation id", operationId, commit.lastCompletedOperationId)
        }

    /**
     * #416 — Verifies that when the atomic write fails during resume, the operation is marked
     * FAILED and lastCompletedOperationId is NOT advanced.
     *
     * Regression: currently there is no try/catch around the write path, so an exception
     * propagates without marking the operation FAILED. This test fails until T004 wraps
     * commitOperation in a try/catch that calls updateOperation(FAILED) on error.
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
}
