package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchReportDataUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var finishMatchUseCase: FinishMatchUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
    private lateinit var startMatchTimerUseCase: StartMatchTimerUseCase
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase
    private lateinit var getMatchSummaryUseCase: GetMatchSummaryUseCase
    private lateinit var getMatchTimelineUseCase: GetMatchTimelineUseCase
    private lateinit var registerGoalUseCase: RegisterGoalUseCase
    private lateinit var startTimeoutUseCase: StartTimeoutUseCase
    private lateinit var endTimeoutUseCase: EndTimeoutUseCase
    private lateinit var getMatchReportDataUseCase: GetMatchReportDataUseCase
    private lateinit var exportMatchReportToPdfUseCase: ExportMatchReportToPdfUseCase
    private lateinit var synchronizeTimeUseCase: SynchronizeTimeUseCase
    private lateinit var startPlayerTimersBatchUseCase: StartPlayerTimersBatchUseCase
    private lateinit var shouldShowInvalidSubstitutionAlertUseCase: ShouldShowInvalidSubstitutionAlertUseCase
    private lateinit var setShouldShowInvalidSubstitutionAlertUseCase: SetShouldShowInvalidSubstitutionAlertUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter
    private lateinit var fakeTicker: FakeTimeTicker

    private val testMatch = Match(
        id = MATCH_ID,
        teamName = "My Team",
        opponent = "Rival FC",
        location = "Stadium",
        periodType = PeriodType.HALF_TIME,
        captainId = 1L,
        squadCallUpIds = listOf(1L, 2L),
        status = MatchStatus.IN_PROGRESS,
    )

    private val players = listOf(
        Player(id = 1L, firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = 1L, isCaptain = false),
        Player(id = 2L, firstName = "Jane", lastName = "Smith", number = 5, positions = listOf(Position.Defender), teamId = 1L, isCaptain = false),
    )

    private val playerTimes = listOf(
        PlayerTime(playerId = 1L, elapsedTimeMillis = 5000L, isRunning = true),
        PlayerTime(playerId = 2L, elapsedTimeMillis = 0L, isRunning = false),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchByIdUseCase = mockk()
        getAllPlayerTimesUseCase = mockk()
        getPlayersUseCase = mockk()
        finishMatchUseCase = mockk(relaxed = true)
        pauseMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
        startMatchTimerUseCase = mockk(relaxed = true)
        registerPlayerSubstitutionUseCase = mockk(relaxed = true)
        getMatchSummaryUseCase = mockk(relaxed = true)
        getMatchTimelineUseCase = mockk(relaxed = true)
        registerGoalUseCase = mockk(relaxed = true)
        startTimeoutUseCase = mockk(relaxed = true)
        endTimeoutUseCase = mockk(relaxed = true)
        getMatchReportDataUseCase = mockk(relaxed = true)
        exportMatchReportToPdfUseCase = mockk(relaxed = true)
        synchronizeTimeUseCase = mockk(relaxed = true)
        startPlayerTimersBatchUseCase = mockk(relaxed = true)
        shouldShowInvalidSubstitutionAlertUseCase = mockk()
        setShouldShowInvalidSubstitutionAlertUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        fakeTicker = FakeTimeTicker()

        every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(testMatch)
        every { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)
        every { getPlayersUseCase() } returns flowOf(players)
        every { shouldShowInvalidSubstitutionAlertUseCase() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MatchViewModel(
        matchId = MATCH_ID,
        getMatchById = getMatchByIdUseCase,
        getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
        getPlayersUseCase = getPlayersUseCase,
        finishMatch = finishMatchUseCase,
        pauseMatch = pauseMatchUseCase,
        resumeMatchUseCase = resumeMatchUseCase,
        startMatchTimerUseCase = startMatchTimerUseCase,
        registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
        getMatchSummaryUseCase = getMatchSummaryUseCase,
        getMatchTimelineUseCase = getMatchTimelineUseCase,
        registerGoal = registerGoalUseCase,
        startTimeoutUseCase = startTimeoutUseCase,
        endTimeoutUseCase = endTimeoutUseCase,
        getMatchReportData = getMatchReportDataUseCase,
        exportMatchReportToPdf = exportMatchReportToPdfUseCase,
        synchronizeTimeUseCase = synchronizeTimeUseCase,
        startPlayerTimersBatchUseCase = startPlayerTimersBatchUseCase,
        shouldShowInvalidSubstitutionAlertUseCase = shouldShowInvalidSubstitutionAlertUseCase,
        setShouldShowInvalidSubstitutionAlertUseCase = setShouldShowInvalidSubstitutionAlertUseCase,
        timeTicker = fakeTicker,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
    )

    @Test
    fun `initial state should be Loading`() {
        val viewModel = createViewModel()
        assertEquals(MatchUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be NoMatch when match does not exist`() = runTest(testDispatcher) {
        every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(null)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(MatchUiState.NoMatch, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when match exists and is in progress`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is MatchUiState.Success)
        assertEquals(testMatch, (state as MatchUiState.Success).match)
    }

    @Test
    fun `Success state should contain only squad call-up players`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as MatchUiState.Success
        assertEquals(2, state.playerTimes.size)
    }

    @Test
    fun `pauseMatch should call PauseMatchUseCase when match has no active period`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.pauseMatch()
        advanceUntilIdle()
        coVerify { pauseMatchUseCase(MATCH_ID, any()) }
    }

    @Test
    fun `dismissPauseConfirmation should clear pause confirmation state`() {
        val viewModel = createViewModel()
        viewModel.dismissPauseConfirmation()
        assertNull(viewModel.showPauseConfirmation.value)
    }

    @Test
    fun `dismissStopConfirmation should hide stop confirmation`() {
        val viewModel = createViewModel()
        viewModel.dismissStopConfirmation()
        assertFalse(viewModel.showStopConfirmation.value)
    }

    @Test
    fun `selectPlayerOut should select running player`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut(1L)
        assertEquals(1L, viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `selectPlayerOut should show invalid substitution alert when player is not running`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut(2L)
        assertTrue(viewModel.showInvalidSubstitutionAlert.value)
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `clearPlayerOutSelection should clear the selected player`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut(1L)
        assertEquals(1L, viewModel.selectedPlayerOut.value)
        viewModel.clearPlayerOutSelection()
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `substitutePlayer should call RegisterPlayerSubstitutionUseCase and clear selection`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut(1L)

        viewModel.substitutePlayer(2L)
        advanceUntilIdle()

        coVerify {
            registerPlayerSubstitutionUseCase(
                matchId = MATCH_ID,
                playerOutId = 1L,
                playerInId = 2L,
                currentTimeMillis = any(),
            )
        }
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `substitutePlayerDirect should call RegisterPlayerSubstitutionUseCase`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.substitutePlayerDirect(playerInId = 2L, playerOutId = 1L)
        advanceUntilIdle()

        coVerify {
            registerPlayerSubstitutionUseCase(
                matchId = MATCH_ID,
                playerOutId = 1L,
                playerInId = 2L,
                currentTimeMillis = any(),
            )
        }
    }

    @Test
    fun `dismissInvalidSubstitutionAlert should hide the alert`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut(2L)
        assertTrue(viewModel.showInvalidSubstitutionAlert.value)

        viewModel.dismissInvalidSubstitutionAlert()

        assertFalse(viewModel.showInvalidSubstitutionAlert.value)
    }

    @Test
    fun `dismissInvalidSubstitutionAlert with dontShowAgain true should call SetShouldShowInvalidSubstitutionAlertUseCase`() {
        val viewModel = createViewModel()
        viewModel.dismissInvalidSubstitutionAlert(dontShowAgain = true)
        verify { setShouldShowInvalidSubstitutionAlertUseCase(false) }
    }

    @Test
    fun `exportCompleted should reset exportState to Idle`() {
        val viewModel = createViewModel()
        viewModel.exportCompleted()
        assertEquals(ExportState.Idle, viewModel.exportState.value)
    }

    @Test
    fun `isSubstitutionInProgress initial value should be false`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.isSubstitutionInProgress.value)
    }

    @Test
    fun `beginMatch should call startMatchTimerUseCase when match is not yet started`() =
        runTest(testDispatcher) {
            val scheduledMatch = testMatch.copy(status = MatchStatus.SCHEDULED)
            every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(scheduledMatch)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.beginMatch(MATCH_ID)
            advanceUntilIdle()

            coVerify { startMatchTimerUseCase(MATCH_ID, any()) }
        }

    @Test
    fun `beginMatch should call startPlayerTimersBatchUseCase when lineup is not empty`() =
        runTest(testDispatcher) {
            val scheduledMatch = testMatch.copy(
                status = MatchStatus.SCHEDULED,
                startingLineupIds = listOf(1L, 2L),
            )
            every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(scheduledMatch)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.beginMatch(MATCH_ID)
            advanceUntilIdle()

            coVerify { startPlayerTimersBatchUseCase(listOf(1L, 2L), any()) }
        }

    @Test
    fun `saveMatch should show stop confirmation when match is not in last period`() =
        runTest(testDispatcher) {
            // testMatch has HALF_TIME with default periods (startTimeMillis=0), so isLastPeriod()=false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.saveMatch()
            advanceUntilIdle()

            assertTrue(viewModel.showStopConfirmation.value)
        }

    @Test
    fun `confirmStopMatch should call finishMatchUseCase`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.confirmStopMatch()
        advanceUntilIdle()

        coVerify { finishMatchUseCase(MATCH_ID, any()) }
    }

    @Test
    fun `resumeMatch should call resumeMatchUseCase`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.resumeMatch(MATCH_ID)
        advanceUntilIdle()

        coVerify { resumeMatchUseCase(MATCH_ID, any()) }
    }

    @Test
    fun `startTimeout should call startTimeoutUseCase when match is in progress`() =
        runTest(testDispatcher) {
            // testMatch.status = IN_PROGRESS, so isInProgress = true
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.startTimeout()
            advanceUntilIdle()

            coVerify { startTimeoutUseCase(MATCH_ID, any()) }
        }

    @Test
    fun `endTimeout should call endTimeoutUseCase when match is in timeout`() =
        runTest(testDispatcher) {
            val timeoutMatch = testMatch.copy(status = MatchStatus.TIMEOUT)
            every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(timeoutMatch)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.endTimeout()
            advanceUntilIdle()

            coVerify { endTimeoutUseCase(MATCH_ID, any()) }
        }

    @Test
    fun `requestExport should set exportState to Ready when export succeeds`() =
        runTest(testDispatcher) {
            val matchReportData = mockk<com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData>()
            every { getMatchReportDataUseCase(MATCH_ID) } returns flowOf(matchReportData)
            coEvery { exportMatchReportToPdfUseCase(matchReportData) } returns "file://report.pdf"
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.requestExport()
            advanceUntilIdle()

            assertEquals(ExportState.Ready("file://report.pdf"), viewModel.exportState.value)
        }

    @Test
    fun `requestExport should set exportState to Error when pdf export returns null`() =
        runTest(testDispatcher) {
            val matchReportData = mockk<com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData>()
            every { getMatchReportDataUseCase(MATCH_ID) } returns flowOf(matchReportData)
            coEvery { exportMatchReportToPdfUseCase(matchReportData) } returns null
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.requestExport()
            advanceUntilIdle()

            assertEquals(ExportState.Error, viewModel.exportState.value)
        }

    @Test
    fun `SubstitutionItem holds correct player and time data`() {
        val playerOut = mockk<Player>()
        val playerIn = mockk<Player>()
        val item = SubstitutionItem(playerOut, playerIn, 60_000L)
        assertEquals(playerOut, item.playerOut)
        assertEquals(playerIn, item.playerIn)
        assertEquals(60_000L, item.matchElapsedTimeMillis)
        // data class equals / copy
        val copy = item.copy(matchElapsedTimeMillis = 90_000L)
        assertEquals(90_000L, copy.matchElapsedTimeMillis)
        assertEquals(playerOut, copy.playerOut)
    }

    @Test
    fun `EndPeriodState holds correct isBreak value`() {
        val breakState = EndPeriodState(isBreak = true)
        val lastPeriodState = EndPeriodState(isBreak = false)
        assertTrue(breakState.isBreak)
        assertFalse(lastPeriodState.isBreak)
        // data class copy
        val copy = breakState.copy(isBreak = false)
        assertFalse(copy.isBreak)
    }

    companion object {
        private const val MATCH_ID = 1L
    }
}

class FakeTimeTicker : TimeTicker {
    private val _flow = MutableSharedFlow<Long>(replay = 1)
    override val timeFlow: Flow<Long> = _flow

    suspend fun emit(time: Long) = _flow.emit(time)
}
