package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.*
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.domain.usecase.*
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getMatchById: GetMatchByIdUseCase = mockk()
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase = mockk()
    private val getPlayersUseCase: GetPlayersUseCase = mockk()
    private val finishMatch: FinishMatchUseCase = mockk(relaxed = true)
    private val pauseMatch: PauseMatchUseCase = mockk(relaxed = true)
    private val resumeMatchUseCase: ResumeMatchUseCase = mockk(relaxed = true)
    private val startMatchTimerUseCase: StartMatchTimerUseCase = mockk(relaxed = true)
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase = mockk(relaxed = true)
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase = mockk()
    private val getMatchTimelineUseCase: GetMatchTimelineUseCase = mockk()
    private val registerGoal: RegisterGoalUseCase = mockk(relaxed = true)
    private val startTimeoutUseCase: StartTimeoutUseCase = mockk(relaxed = true)
    private val endTimeoutUseCase: EndTimeoutUseCase = mockk(relaxed = true)
    private val getMatchReportData: GetMatchReportDataUseCase = mockk()
    private val exportMatchReportToPdf: ExportMatchReportToPdfUseCase = mockk()
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase = mockk(relaxed = true)
    private val startPlayerTimersBatchUseCase: StartPlayerTimersBatchUseCase = mockk(relaxed = true)
    private val shouldShowInvalidSubstitutionAlertUseCase: ShouldShowInvalidSubstitutionAlertUseCase = mockk()
    private val setShouldShowInvalidSubstitutionAlertUseCase: SetShouldShowInvalidSubstitutionAlertUseCase = mockk(relaxed = true)
    private val timeTicker: TimeTicker = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val crashReporter: CrashReporter = mockk(relaxed = true)

    private val timeFlow = MutableSharedFlow<Long>(replay = 1)
    private lateinit var viewModel: MatchViewModel

    private val matchId = 1L
    private val match = Match(
        id = matchId,
        teamName = "My Team",
        opponent = "Opponent",
        location = "Location",
        periodType = PeriodType.HALF_TIME,
        captainId = 1L,
        status = MatchStatus.SCHEDULED,
        squadCallUpIds = listOf(1L, 2L),
        startingLineupIds = listOf(1L, 2L)
    )

    private val matchFlow = MutableStateFlow<Match?>(match)
    private val playerTimesFlow = MutableStateFlow<List<PlayerTime>>(emptyList())
    private val playersFlow = MutableStateFlow<List<Player>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { timeTicker.timeFlow } returns timeFlow
        every { getMatchById(matchId) } returns matchFlow
        every { getAllPlayerTimesUseCase() } returns playerTimesFlow
        every { getPlayersUseCase() } returns playersFlow
        every { shouldShowInvalidSubstitutionAlertUseCase() } returns true

        timeFlow.tryEmit(0L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MatchViewModel(
        getMatchById,
        getAllPlayerTimesUseCase,
        getPlayersUseCase,
        finishMatch,
        pauseMatch,
        resumeMatchUseCase,
        startMatchTimerUseCase,
        registerPlayerSubstitutionUseCase,
        getMatchSummaryUseCase,
        getMatchTimelineUseCase,
        registerGoal,
        startTimeoutUseCase,
        endTimeoutUseCase,
        getMatchReportData,
        exportMatchReportToPdf,
        synchronizeTimeUseCase,
        startPlayerTimersBatchUseCase,
        shouldShowInvalidSubstitutionAlertUseCase,
        setShouldShowInvalidSubstitutionAlertUseCase,
        timeTicker,
        analyticsTracker,
        crashReporter,
        SavedStateHandle(mapOf(Route.Match.ARG_MATCH_ID to matchId))
    )

    @Test
    fun `initial state should be Loading`() = runTest {
        viewModel = createViewModel()
        assertEquals(MatchUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `beginMatch should call startMatchTimerUseCase and startPlayerTimersBatchUseCase`() = runTest {
        // Given
        viewModel = createViewModel()
        val currentTime = 1000L

        // Let it load
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue("State should be Success but was $currentState", currentState is MatchUiState.Success)

        // When
        timeFlow.emit(currentTime)
        runCurrent()

        viewModel.beginMatch(matchId)
        advanceUntilIdle()

        // Then
        coVerify { startMatchTimerUseCase(matchId, any()) }
        coVerify { startPlayerTimersBatchUseCase(listOf(1L, 2L), any()) }
    }

    @Test
    fun `pauseMatch should call pauseMatch use case`() = runTest {
        // Given
        val runningMatch = match.copy(status = MatchStatus.IN_PROGRESS)
        matchFlow.value = runningMatch

        viewModel = createViewModel()
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue("State should be Success but was $currentState", currentState is MatchUiState.Success)

        // When
        viewModel.pauseMatch()
        advanceUntilIdle()

        // Then
        coVerify { pauseMatch(matchId, any()) }
    }

    @Test
    fun `registerGoal should call registerGoal use case`() = runTest {
        // Given
        val runningMatch = match.copy(status = MatchStatus.IN_PROGRESS)
        matchFlow.value = runningMatch

        viewModel = createViewModel()
        advanceUntilIdle()

        val currentTime = 5000L
        timeFlow.emit(currentTime)
        runCurrent()

        // When
        viewModel.registerGoal(1L)
        advanceUntilIdle()

        // Then
        coVerify {
            registerGoal(
                matchId = matchId,
                scorerId = 1L,
                currentTimeMillis = currentTime,
                isOpponentGoal = false,
                isOwnGoal = false
            )
        }
    }

    @Test
    fun `registerOpponentGoal should call registerGoal use case`() = runTest {
        // Given
        val runningMatch = match.copy(status = MatchStatus.IN_PROGRESS)
        matchFlow.value = runningMatch

        viewModel = createViewModel()
        advanceUntilIdle()

        val currentTime = 6000L
        timeFlow.emit(currentTime)
        runCurrent()

        // When
        viewModel.registerOpponentGoal()
        advanceUntilIdle()

        // Then
        coVerify {
            registerGoal(
                matchId = matchId,
                scorerId = null,
                currentTimeMillis = currentTime,
                isOpponentGoal = true,
                isOwnGoal = false
            )
        }
    }

    @Test
    fun `substitutePlayer should call registerPlayerSubstitutionUseCase`() = runTest {
        // Given
        val players = listOf(
            Player(1L, "P1", "L1", 1, emptyList(), 1L, false),
            Player(2L, "P2", "L2", 2, emptyList(), 1L, false)
        )
        playersFlow.value = players

        val playerTimes = listOf(
            PlayerTime(1L, 0L, true, 1000L, PlayerTimeStatus.PLAYING),
            PlayerTime(2L, 0L, false, null, PlayerTimeStatus.ON_BENCH)
        )
        playerTimesFlow.value = playerTimes

        val runningMatch = match.copy(status = MatchStatus.IN_PROGRESS, squadCallUpIds = listOf(1L, 2L))
        matchFlow.value = runningMatch

        viewModel = createViewModel()
        advanceUntilIdle()

        val currentTime = 7000L
        timeFlow.emit(currentTime)
        runCurrent()

        // When
        viewModel.selectPlayerOut(1L)
        viewModel.substitutePlayer(2L)
        advanceUntilIdle()

        // Then
        coVerify {
            registerPlayerSubstitutionUseCase(
                matchId = matchId,
                playerOutId = 1L,
                playerInId = 2L,
                currentTimeMillis = currentTime
            )
        }
    }
}
