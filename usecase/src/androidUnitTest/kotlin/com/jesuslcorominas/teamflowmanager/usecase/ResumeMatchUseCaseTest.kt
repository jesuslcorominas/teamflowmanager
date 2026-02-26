package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
            val matchId = 1L
            val currentTime = 1000L
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(null)

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
        }

    @Test
    fun `givenMatchPausedWithPausedPlayers_whenInvoke_thenResumeAllPausedPlayersAndMatch`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val match = createPausedMatch(matchId)
            val playerTimes = listOf(
                PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED),
                PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PAUSED),
                PlayerTime(playerId = 3L, isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { playerTimeRepository.startTimersBatchWithOperationId(listOf(1L, 2L), currentTime, "op1") }
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
            val matchId = 1L
            val currentTime = 1000L
            val match = createPausedMatch(matchId)
            val playerTimes = listOf(
                PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
                PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
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
            val matchId = 1L
            val currentTime = 1000L
            val match = createPausedMatch(matchId)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(emptyList())

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.IN_PROGRESS }
                )
            }
        }

    private fun createPausedMatch(id: Long): Match {
        return Match(
            id = id,
            teamId = 1L,
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            status = MatchStatus.PAUSED,
            periods = listOf(
                // First period finished
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 1501000L),
                // Second period not started yet
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
    }
}
