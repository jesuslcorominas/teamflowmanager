package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
            val matchId = 1L
            val currentTime = 1000L
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(null)

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
        }

    @Test
    fun `givenMatchWithStartingLineup_whenInvoke_thenStartMatchAndTimers`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val startingLineup = listOf(10L, 11L, 12L)
            val match = createScheduledMatch(matchId, startingLineupIds = startingLineup)
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.IN_PROGRESS }
                )
            }
            coVerify { playerTimeRepository.startTimersBatchWithOperationId(startingLineup, currentTime, "op1") }
        }

    @Test
    fun `givenMatchWithNoStartingLineup_whenInvoke_thenOnlyStartMatch`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val match = createScheduledMatch(matchId, startingLineupIds = emptyList())
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match { it.status == MatchStatus.IN_PROGRESS }
                )
            }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
        }

    @Test
    fun `givenAllPeriodsAlreadyStarted_whenInvoke_thenDoNotUpdateMatch`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 3000L
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
                    MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 1000L, endTimeMillis = 1501000L),
                    MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 2000L, endTimeMillis = 0L),
                ),
            )
            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
        }

    private fun createScheduledMatch(id: Long, startingLineupIds: List<Long> = emptyList()): Match {
        return Match(
            id = id,
            teamId = 1L,
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            status = MatchStatus.SCHEDULED,
            startingLineupIds = startingLineupIds,
            periods = listOf(
                MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
    }
}
