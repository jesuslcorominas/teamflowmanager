package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.DiscardedSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionBatchResult
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionDiscardReason
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchReportDataUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
    private lateinit var notifyPresidentMatchEventUseCase: NotifyPresidentMatchEventUseCase
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getPlayersByTeamUseCase: GetPlayersByTeamUseCase
    private lateinit var observeSubstitutionModeUseCase: ObserveSubstitutionModeUseCase
    private lateinit var observePendingSubstitutionsUseCase: ObservePendingSubstitutionsUseCase
    private lateinit var addPendingSubstitutionUseCase: AddPendingSubstitutionUseCase
    private lateinit var removePendingSubstitutionUseCase: RemovePendingSubstitutionUseCase
    private lateinit var clearPendingSubstitutionsUseCase: ClearPendingSubstitutionsUseCase

    /** Stands in for the pending store so a test can watch what the ViewModel schedules. */
    private lateinit var pendingStore: MutableStateFlow<List<SubstitutionPair>>

    private val testMatch = Match(
        id = MATCH_ID,
        teamName = "My Team",
        opponent = "Rival FC",
        location = "Stadium",
        periodType = PeriodType.HALF_TIME,
        captainId = "1",
        squadCallUpIds = listOf("1", "2"),
        status = MatchStatus.IN_PROGRESS,
    )

    private val players = listOf(
        Player(id = "1", firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = "1", isCaptain = false),
        Player(id = "2", firstName = "Jane", lastName = "Smith", number = 5, positions = listOf(Position.Defender), teamId = "1", isCaptain = false),
    )

    private val playerTimes = listOf(
        PlayerTime(playerId = "1", elapsedTimeMillis = 5000L, isRunning = true),
        PlayerTime(playerId = "2", elapsedTimeMillis = 0L, isRunning = false),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchByIdUseCase = mockk()
        getAllPlayerTimesUseCase = mockk()
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
        notifyPresidentMatchEventUseCase = mockk(relaxed = true)
        getTeamUseCase = mockk(relaxed = true)
        getPlayersByTeamUseCase = mockk(relaxed = true)
        observeSubstitutionModeUseCase = mockk()
        observePendingSubstitutionsUseCase = mockk()
        addPendingSubstitutionUseCase = mockk(relaxed = true)
        removePendingSubstitutionUseCase = mockk(relaxed = true)
        clearPendingSubstitutionsUseCase = mockk(relaxed = true)
        pendingStore = MutableStateFlow(emptyList())

        // Live mode and an empty queue by default: every pre-existing test keeps exercising the
        // immediate path it was written for. The scheduled tests override these explicitly.
        every { observeSubstitutionModeUseCase() } returns flowOf(SubstitutionMode.LIVE)
        every { observePendingSubstitutionsUseCase(any()) } returns pendingStore

        every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(testMatch)
        every { getAllPlayerTimesUseCase(any()) } returns flowOf(playerTimes)
        every { getPlayersByTeamUseCase(any()) } returns flowOf(players)
        every { getMatchTimelineUseCase(any()) } returns flowOf(null)
        every { shouldShowInvalidSubstitutionAlertUseCase() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MatchViewModel(
        matchId = MATCH_ID,
        clock =
            MatchClockController(
                startMatchTimerUseCase = startMatchTimerUseCase,
                startPlayerTimersBatchUseCase = startPlayerTimersBatchUseCase,
                synchronizeTimeUseCase = synchronizeTimeUseCase,
                pauseMatchUseCase = pauseMatchUseCase,
                resumeMatchUseCase = resumeMatchUseCase,
                finishMatchUseCase = finishMatchUseCase,
                startTimeoutUseCase = startTimeoutUseCase,
                endTimeoutUseCase = endTimeoutUseCase,
                getMatchById = getMatchByIdUseCase,
                analyticsTracker = analyticsTracker,
                crashReporter = crashReporter,
            ),
        substitutions =
            MatchSubstitutionCoordinator(
                matchId = MATCH_ID,
                registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
                observeSubstitutionModeUseCase = observeSubstitutionModeUseCase,
                observePendingSubstitutionsUseCase = observePendingSubstitutionsUseCase,
                addPendingSubstitutionUseCase = addPendingSubstitutionUseCase,
                removePendingSubstitutionUseCase = removePendingSubstitutionUseCase,
                clearPendingSubstitutionsUseCase = clearPendingSubstitutionsUseCase,
                shouldShowInvalidSubstitutionAlertUseCase = shouldShowInvalidSubstitutionAlertUseCase,
                setShouldShowInvalidSubstitutionAlertUseCase = setShouldShowInvalidSubstitutionAlertUseCase,
                analyticsTracker = analyticsTracker,
                crashReporter = crashReporter,
            ),
        stateLoader =
            MatchStateLoader(
                getMatchById = getMatchByIdUseCase,
                getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
                getPlayersByTeamUseCase = getPlayersByTeamUseCase,
                getMatchTimelineUseCase = getMatchTimelineUseCase,
                getMatchSummaryUseCase = getMatchSummaryUseCase,
            ),
        goalRecorder =
            MatchGoalRecorder(
                registerGoal = registerGoalUseCase,
                getMatchById = getMatchByIdUseCase,
                analyticsTracker = analyticsTracker,
                crashReporter = crashReporter,
            ),
        reportExporter =
            MatchReportExporter(
                getMatchReportData = getMatchReportDataUseCase,
                exportMatchReportToPdf = exportMatchReportToPdfUseCase,
                analyticsTracker = analyticsTracker,
                crashReporter = crashReporter,
            ),
        timeTicker = fakeTicker,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
        notifyPresidentMatchEvent = notifyPresidentMatchEventUseCase,
        getTeamUseCase = getTeamUseCase,
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
        viewModel.selectPlayerOut("1")
        assertEquals("1", viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `selectPlayerOut should show invalid substitution alert when player is not running`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("2")
        assertTrue(viewModel.showInvalidSubstitutionAlert.value)
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `clearPlayerOutSelection should clear the selected player`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("1")
        assertEquals("1", viewModel.selectedPlayerOut.value)
        viewModel.clearPlayerOutSelection()
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `substitutePlayer should call RegisterPlayerSubstitutionUseCase and clear selection`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("1")

        viewModel.substitutePlayer("2")
        advanceUntilIdle()

        coVerify {
            registerPlayerSubstitutionUseCase(
                matchId = MATCH_ID,
                substitutions = listOf(SubstitutionPair(playerOutId = "1", playerInId = "2")),
                currentTimeMillis = any(),
            )
        }
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `substitutePlayerDirect should call RegisterPlayerSubstitutionUseCase`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.substitutePlayerDirect(playerInId = "2", playerOutId = "1")
        advanceUntilIdle()

        coVerify {
            registerPlayerSubstitutionUseCase(
                matchId = MATCH_ID,
                substitutions = listOf(SubstitutionPair(playerOutId = "1", playerInId = "2")),
                currentTimeMillis = any(),
            )
        }
    }

    @Test
    fun `dismissInvalidSubstitutionAlert should hide the alert`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("2")
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
                startingLineupIds = listOf("1", "2"),
            )
            every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(scheduledMatch)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.beginMatch(MATCH_ID)
            advanceUntilIdle()

            coVerify { startPlayerTimersBatchUseCase(MATCH_ID, listOf("1", "2"), any()) }
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


    // ── Scheduled substitutions (#412) ───────────────────────────────────────

    /** Four called-up players, two of them on the pitch, so a batch of two pairs is possible. */
    private fun givenFourPlayerSquad(playerTimeStatus: PlayerTimeStatus = PlayerTimeStatus.PLAYING) {
        val squad = listOf(
            Player(id = "1", firstName = "A", lastName = "A", number = 1, positions = listOf(Position.Forward), teamId = "1", isCaptain = false),
            Player(id = "2", firstName = "B", lastName = "B", number = 2, positions = listOf(Position.Defender), teamId = "1", isCaptain = false),
            Player(id = "3", firstName = "C", lastName = "C", number = 3, positions = listOf(Position.Forward), teamId = "1", isCaptain = false),
            Player(id = "4", firstName = "D", lastName = "D", number = 4, positions = listOf(Position.Defender), teamId = "1", isCaptain = false),
        )
        val onPitch = playerTimeStatus == PlayerTimeStatus.PLAYING
        every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(
            testMatch.copy(
                squadCallUpIds = listOf("1", "2", "3", "4"),
                status = if (onPitch) MatchStatus.IN_PROGRESS else MatchStatus.PAUSED,
            ),
        )
        every { getPlayersByTeamUseCase(any()) } returns flowOf(squad)
        every { getAllPlayerTimesUseCase(any()) } returns flowOf(
            listOf(
                PlayerTime(playerId = "1", elapsedTimeMillis = 5000L, isRunning = onPitch, status = playerTimeStatus),
                PlayerTime(playerId = "3", elapsedTimeMillis = 5000L, isRunning = onPitch, status = playerTimeStatus),
                PlayerTime(playerId = "2", elapsedTimeMillis = 0L, isRunning = false, status = PlayerTimeStatus.ON_BENCH),
                PlayerTime(playerId = "4", elapsedTimeMillis = 0L, isRunning = false, status = PlayerTimeStatus.ON_BENCH),
            ),
        )
    }

    private fun givenScheduledMode() {
        every { observeSubstitutionModeUseCase() } returns flowOf(SubstitutionMode.SCHEDULED)
    }

    @Test
    fun `givenScheduledMode_whenSubstitutePlayer_thenQueuesTheCardInsteadOfApplyingIt`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.selectPlayerOut("1")
        viewModel.substitutePlayer("2")
        advanceUntilIdle()

        // Then — this is the whole point of the feature: nothing is applied yet
        verify(exactly = 1) { addPendingSubstitutionUseCase(MATCH_ID, PAIR_1_2) }
        coVerify(exactly = 0) { registerPlayerSubstitutionUseCase(any(), any(), any()) }
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `givenLiveMode_whenSubstitutePlayer_thenAppliesImmediatelyAndQueuesNothing`() = runTest(testDispatcher) {
        // Given — live mode is the default of this suite; stated here because it is the subject
        every { observeSubstitutionModeUseCase() } returns flowOf(SubstitutionMode.LIVE)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.selectPlayerOut("1")
        viewModel.substitutePlayer("2")
        advanceUntilIdle()

        // Then — guards the non-regression the issue asks for
        coVerify(exactly = 1) { registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2), any()) }
        verify(exactly = 0) { addPendingSubstitutionUseCase(any(), any()) }
    }

    @Test
    fun `givenAQueuedCard_whenExecuteIt_thenRunsABatchOfOneAndUnschedulesIt`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        coEvery { registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2), any()) } returns
            SubstitutionBatchResult(applied = listOf(PAIR_1_2), discarded = emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.executePendingSubstitution(PAIR_1_2)
        advanceUntilIdle()

        // Then — a single card takes the same path as a batch, with a list of one
        coVerify(exactly = 1) { registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2), any()) }
        verify(exactly = 1) { removePendingSubstitutionUseCase(MATCH_ID, PAIR_1_2) }
        assertEquals(listOf(PAIR_1_2), viewModel.lastSubstitutionResult.value?.applied?.map { it.pair })
    }

    @Test
    fun `givenTwoQueuedCards_whenExecuteAll_thenRunsThemInASingleBatch`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_2, PAIR_3_4)
        coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
            SubstitutionBatchResult(applied = listOf(PAIR_1_2, PAIR_3_4), discarded = emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.executeAllPendingSubstitutions()
        advanceUntilIdle()

        // Then — ONE invocation carrying both pairs: that is what gives them one operation id.
        // Two invocations of one pair each would also "work" and would be wrong.
        coVerify(exactly = 1) { registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2, PAIR_3_4), any()) }
        coVerify(exactly = 1) { registerPlayerSubstitutionUseCase(any(), any(), any()) }
    }

    @Test
    fun `givenQueuedCards_whenRemoveOneOrClearAll_thenTheStoreIsToldExactlyThat`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.removePendingSubstitution(PAIR_1_2)
        viewModel.clearPendingSubstitutions()

        // Then
        verify(exactly = 1) { removePendingSubstitutionUseCase(MATCH_ID, PAIR_1_2) }
        verify(exactly = 1) { clearPendingSubstitutionsUseCase(MATCH_ID) }
    }

    @Test
    fun `givenAQueuedPlayer_whenPickedToComeOff_thenWarnsAtThatMomentAndSelectsNobodyYet`() = runTest(testDispatcher) {
        // Given — player 1 already has a change waiting
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When — they are picked as the player coming off, and nothing more
        viewModel.selectPlayerOut("1")
        advanceUntilIdle()

        // Then — asked straight away, about the only player picked so far, and not yet selected:
        // this is the whole point of moving the question off the save.
        assertEquals("1", viewModel.playerAlreadyScheduledAlert.value?.playerId)
        assertNull(viewModel.selectedPlayerOut.value)
        verify(exactly = 0) { addPendingSubstitutionUseCase(any(), any()) }
    }

    @Test
    fun `givenTheWarningAboutThePlayerComingOff_whenConfirmed_thenHeIsSelectedAndNothingIsWrittenYet`() =
        runTest(testDispatcher) {
            // Given
            givenScheduledMode()
            givenFourPlayerSquad()
            pendingStore.value = listOf(PAIR_1_4)
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.selectPlayerOut("1")
            advanceUntilIdle()

            // When
            viewModel.confirmPlayerAlreadyScheduled()
            advanceUntilIdle()

            // Then — confirming picks the player; who comes on is still an open question
            assertNull(viewModel.playerAlreadyScheduledAlert.value)
            assertEquals("1", viewModel.selectedPlayerOut.value)
            verify(exactly = 0) { addPendingSubstitutionUseCase(any(), any()) }
        }

    @Test
    fun `givenTheWarningWasAcceptedForThePlayerComingOff_whenTheOtherIsPicked_thenItIsNotAskedAgain`() =
        runTest(testDispatcher) {
            // Given — the coach has already said yes about player 1
            givenScheduledMode()
            givenFourPlayerSquad()
            pendingStore.value = listOf(PAIR_1_4)
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.selectPlayerOut("1")
            viewModel.confirmPlayerAlreadyScheduled()
            advanceUntilIdle()

            // When — player 2, who has no change of their own, comes on
            viewModel.substitutePlayer("2")
            advanceUntilIdle()

            // Then — written without a second dialog. A permission already granted is not asked
            // for again; that is how people learn to dismiss dialogs unread.
            assertNull(viewModel.playerAlreadyScheduledAlert.value)
            verify(exactly = 1) { addPendingSubstitutionUseCase(MATCH_ID, PAIR_1_2) }
        }

    @Test
    fun `givenTheWarningAboutThePlayerComingOff_whenDismissed_thenNobodyIsSelected`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("1")
        advanceUntilIdle()

        // When
        viewModel.dismissPlayerAlreadyScheduled()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.playerAlreadyScheduledAlert.value)
        assertNull(viewModel.selectedPlayerOut.value)
        verify(exactly = 0) { addPendingSubstitutionUseCase(any(), any()) }
    }

    @Test
    fun `givenAQueuedPlayer_whenPickedToComeOn_thenWarnsAndKeepsTheFirstPick`() = runTest(testDispatcher) {
        // Given — player 4 is the incoming half of a queued change; player 3 has none
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("3")
        advanceUntilIdle()

        // When
        viewModel.substitutePlayer("4")
        advanceUntilIdle()

        // Then — the warning names the player just tapped, not the one picked earlier, and nothing
        // is written. The first pick survives: the coach was told this player is taken, not that
        // they should start over.
        assertEquals("4", viewModel.playerAlreadyScheduledAlert.value?.playerId)
        assertEquals("3", viewModel.selectedPlayerOut.value)
        verify(exactly = 0) { addPendingSubstitutionUseCase(any(), any()) }
    }

    @Test
    fun `givenTheWarningAboutThePlayerComingOn_whenDismissed_thenTheFirstPickSurvives`() = runTest(testDispatcher) {
        // Given — player 3 is already chosen to come off, and player 4 turns out to be taken
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("3")
        viewModel.substitutePlayer("4")
        advanceUntilIdle()

        // When — the coach backs out of the warning
        viewModel.dismissPlayerAlreadyScheduled()
        advanceUntilIdle()

        // Then — player 3 is STILL chosen. This is the branch the other dismissal test cannot
        // reach: there the warning is about the player coming off, so selectedPlayerOut is null
        // before and after and the assertion passes for either reason. Here it is null only if the
        // dismissal wrongly threw the first pick away, which is the thing being claimed.
        assertNull(viewModel.playerAlreadyScheduledAlert.value)
        assertEquals("3", viewModel.selectedPlayerOut.value)
        verify(exactly = 0) { addPendingSubstitutionUseCase(any(), any()) }
    }

    @Test
    fun `givenTheWarningAboutThePlayerComingOn_whenConfirmed_thenThePairIsWritten`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectPlayerOut("3")
        viewModel.substitutePlayer("4")
        advanceUntilIdle()

        // When
        viewModel.confirmPlayerAlreadyScheduled()
        advanceUntilIdle()

        // Then — the store drops the pair this displaces; the ViewModel only writes the new one
        assertNull(viewModel.playerAlreadyScheduledAlert.value)
        verify(exactly = 1) { addPendingSubstitutionUseCase(MATCH_ID, PAIR_3_4) }
        assertNull(viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `givenTwoPlayersWithNoChangeOfTheirOwn_whenScheduled_thenNothingIsAskedAtAll`() = runTest(testDispatcher) {
        // Given — a queue that involves neither of the two about to be picked
        givenScheduledMode()
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.selectPlayerOut("3")
        viewModel.substitutePlayer("2")
        advanceUntilIdle()

        // Then — straight through, no dialog on the way in and none on the way out
        assertNull(viewModel.playerAlreadyScheduledAlert.value)
        verify(exactly = 1) { addPendingSubstitutionUseCase(MATCH_ID, SubstitutionPair(playerOutId = "3", playerInId = "2")) }
    }

    @Test
    fun `givenLiveMode_whenPickingAPlayerWithAQueuedChange_thenNothingIsAsked`() = runTest(testDispatcher) {
        // Given — a queue that outlived a switch to live. Nothing here displaces anything: a live
        // substitution applies at once and leaves the queue alone, so a warning would be noise.
        givenFourPlayerSquad()
        pendingStore.value = listOf(PAIR_1_4)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.selectPlayerOut("1")
        advanceUntilIdle()

        // Then
        assertNull(viewModel.playerAlreadyScheduledAlert.value)
        assertEquals("1", viewModel.selectedPlayerOut.value)
    }

    @Test
    fun `givenAPausedMatch_whenSelectingAPausedPlayer_thenScheduledModeAcceptsHimAndLiveModeDoesNot`() = runTest(testDispatcher) {
        // Given — during the break the pitch players sit at PAUSED, not PLAYING
        givenScheduledMode()
        givenFourPlayerSquad(playerTimeStatus = PlayerTimeStatus.PAUSED)
        val scheduled = createViewModel()
        advanceUntilIdle()

        // When
        scheduled.selectPlayerOut("1")

        // Then — the coach can queue changes during the break
        assertEquals("1", scheduled.selectedPlayerOut.value)
        assertFalse(scheduled.showInvalidSubstitutionAlert.value)

        // Given — same state, live mode
        every { observeSubstitutionModeUseCase() } returns flowOf(SubstitutionMode.LIVE)
        val live = createViewModel()
        advanceUntilIdle()

        // When
        live.selectPlayerOut("1")

        // Then — live keeps rejecting: applying it now would be discarded by the use case anyway,
        // and unlike a queued card there would be nothing left to recover
        assertNull(live.selectedPlayerOut.value)
        assertTrue(live.showInvalidSubstitutionAlert.value)
    }

    @Test
    fun `givenQueuedCards_whenResuming_thenRestoresThePlayersBeforeRunningTheBatch`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        pendingStore.value = listOf(PAIR_1_2)
        coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
            SubstitutionBatchResult(applied = listOf(PAIR_1_2), discarded = emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.resumeMatch(MATCH_ID)
        advanceUntilIdle()

        // Then — ResumeMatchUseCase puts the paused players back to PLAYING, and the register use
        // case selects on PLAYING. The reverse order discards the whole batch silently.
        coVerifyOrder {
            resumeMatchUseCase(MATCH_ID, any())
            registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2), any())
        }
    }

    @Test
    fun `givenResumeDiscardsEverything_thenTheCardsAreDroppedAndTheResultSaysWhy`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        pendingStore.value = listOf(PAIR_1_2)
        coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
            SubstitutionBatchResult(
                applied = emptyList(),
                discarded = listOf(DiscardedSubstitution(PAIR_1_2, SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING)),
            )
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.resumeMatch(MATCH_ID)
        advanceUntilIdle()

        // Then — a card that outlived a resume would fire again by itself at the next break
        verify(exactly = 1) { removePendingSubstitutionUseCase(MATCH_ID, PAIR_1_2) }
        val result = viewModel.lastSubstitutionResult.value
        assertEquals(SubstitutionExecutionTrigger.RESUME, result?.trigger)
        assertTrue(result?.applied?.isEmpty() == true)
        assertEquals(SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING, result?.discarded?.single()?.reason)
    }

    @Test
    fun `givenAManualRunThatDiscards_thenTheCardIsKeptForTheCoach`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
            SubstitutionBatchResult(
                applied = emptyList(),
                discarded = listOf(DiscardedSubstitution(PAIR_1_2, SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING)),
            )
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.executePendingSubstitution(PAIR_1_2)
        advanceUntilIdle()

        // Then — with the coach watching, deleting their card would lose work with no way back
        verify(exactly = 0) { removePendingSubstitutionUseCase(any(), any()) }
        assertEquals(SubstitutionExecutionTrigger.MANUAL, viewModel.lastSubstitutionResult.value?.trigger)
    }

    @Test
    fun `givenAPausedMatch_whenExecutingACard_thenItRunsTheCardIsKeptAndTheResultExplainsIt`() = runTest(testDispatcher) {
        // Given — the coach queued during the break and pressed the button there too
        givenScheduledMode()
        givenFourPlayerSquad(playerTimeStatus = PlayerTimeStatus.PAUSED)
        pendingStore.value = listOf(PAIR_1_2)
        coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
            SubstitutionBatchResult(
                applied = emptyList(),
                discarded = listOf(DiscardedSubstitution(PAIR_1_2, SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING)),
            )
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.executeAllPendingSubstitutions()
        advanceUntilIdle()

        // Then — the button is not blocked, nothing applies while paused, and nothing is lost:
        // the card stays and will run on its own when the match resumes
        coVerify(exactly = 1) { registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2), any()) }
        verify(exactly = 0) { removePendingSubstitutionUseCase(any(), any()) }
        assertEquals(
            SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING,
            viewModel.lastSubstitutionResult.value?.discarded?.single()?.reason,
        )
    }

    @Test
    fun `givenQueuedCards_whenTheMatchIsFinished_thenTheQueueIsEmptied`() = runTest(testDispatcher) {
        // Given
        givenScheduledMode()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.confirmStopMatch()
        advanceUntilIdle()

        // Then — a finished match takes no more substitutions
        coVerify(exactly = 1) { finishMatchUseCase(MATCH_ID, any()) }
        verify(exactly = 1) { clearPendingSubstitutionsUseCase(MATCH_ID) }
    }


    @Test
    fun `givenQueuedPairs_thenPendingSubstitutionsResolvesThemToPlayersInOrder`() =
        runTest(testDispatcher) {
            // Given
            givenScheduledMode()
            givenFourPlayerSquad()
            pendingStore.value = listOf(PAIR_3_4, PAIR_1_2)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then — this is what B5 paints: names and numbers, in the order they were queued
            val items = viewModel.pendingSubstitutions.value
            assertEquals(listOf(PAIR_3_4, PAIR_1_2), items.map { it.pair })
            assertEquals(listOf("3", "1"), items.map { it.playerOut.id })
            assertEquals(listOf(4, 2), items.map { it.playerIn.number })
        }

    @Test
    fun `givenAPairWhosePlayersAreNotCalledUp_thenNoBlankCardIsEmitted`() =
        runTest(testDispatcher) {
            // Given — the squad is only players 1 and 2
            givenScheduledMode()
            pendingStore.value = listOf(PAIR_1_2, PAIR_3_4)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then — an unpaintable pair is dropped rather than shown empty
            assertEquals(listOf(PAIR_1_2), viewModel.pendingSubstitutions.value.map { it.pair })
        }

    @Test
    fun `givenAReportedResult_whenConsumed_thenItIsCleared`() =
        runTest(testDispatcher) {
            // Given
            givenScheduledMode()
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
                SubstitutionBatchResult(applied = listOf(PAIR_1_2), discarded = emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.executePendingSubstitution(PAIR_1_2)
            advanceUntilIdle()
            assertTrue(viewModel.lastSubstitutionResult.value != null)

            // When
            viewModel.consumeLastSubstitutionResult()

            // Then — the screen acknowledges it so a rotation does not show it twice
            assertNull(viewModel.lastSubstitutionResult.value)
        }

    @Test
    fun `givenAnEmptyQueue_whenExecuteAll_thenNothingIsAttempted`() =
        runTest(testDispatcher) {
            // Given
            givenScheduledMode()
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.executeAllPendingSubstitutions()
            advanceUntilIdle()

            // Then — no empty operation, and no result banner out of nowhere
            coVerify(exactly = 0) { registerPlayerSubstitutionUseCase(any(), any(), any()) }
            assertNull(viewModel.lastSubstitutionResult.value)
        }


    @Test
    fun `givenAResumeRunThatUnschedulesTheCards_thenTheResultStillNamesThePlayers`() =
        runTest(testDispatcher) {
            // Given
            givenScheduledMode()
            givenFourPlayerSquad()
            pendingStore.value = listOf(PAIR_1_2, PAIR_3_4)
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
                SubstitutionBatchResult(
                    applied = listOf(PAIR_1_2),
                    discarded = listOf(DiscardedSubstitution(PAIR_3_4, SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH)),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.resumeMatch(MATCH_ID)
            advanceUntilIdle()

            // Then — the cards are gone from the store by now, so the result is the only place
            // left that can tell the coach who came on and who went off
            verify { removePendingSubstitutionUseCase(MATCH_ID, PAIR_1_2) }
            verify { removePendingSubstitutionUseCase(MATCH_ID, PAIR_3_4) }
            val result = viewModel.lastSubstitutionResult.value
            assertEquals("1", result?.applied?.single()?.playerOut?.id)
            assertEquals(2, result?.applied?.single()?.playerIn?.number)
            val discarded = result?.discarded?.single()
            assertEquals("3", discarded?.substitution?.playerOut?.id)
            assertEquals(4, discarded?.substitution?.playerIn?.number)
            assertEquals(SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH, discarded?.reason)
        }

    @Test
    fun `givenTheSquadHasNotLoadedYet_whenExecuteAll_thenTheQueuedCardsStillRun`() =
        runTest(testDispatcher) {
            // Given — the store holds a card but the squad never arrives, so the resolved list
            // stays empty. This is the window that exists while the player flows are loading.
            givenScheduledMode()
            every { getPlayersByTeamUseCase(any()) } returns emptyFlow()
            pendingStore.value = listOf(PAIR_1_2)
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
                SubstitutionBatchResult(applied = listOf(PAIR_1_2), discarded = emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.pendingSubstitutions.value.isEmpty())

            // When
            viewModel.executeAllPendingSubstitutions()
            advanceUntilIdle()

            // Then — reading the store rather than the resolved list keeps the button from
            // being a silent no-op here
            coVerify(exactly = 1) { registerPlayerSubstitutionUseCase(MATCH_ID, listOf(PAIR_1_2), any()) }
        }

    @Test
    fun `givenTheBatchThrowsOnAManualRun_thenItIsReportedAndTheSpinnerIsCleared`() =
        runTest(testDispatcher) {
            // Given
            givenScheduledMode()
            val boom = IllegalStateException("no active match found")
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } throws boom
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.executePendingSubstitution(PAIR_1_2)
            runCatching { advanceUntilIdle() }

            // Then — the manual path used to be the only substitution route blind to diagnostics
            verify { crashReporter.recordException(boom) }
            assertFalse(viewModel.isSubstitutionInProgress.value)
        }

    companion object {
        private const val MATCH_ID = "1"
        private val PAIR_1_2 = SubstitutionPair(playerOutId = "1", playerInId = "2")
        private val PAIR_3_4 = SubstitutionPair(playerOutId = "3", playerInId = "4")
        private val PAIR_1_4 = SubstitutionPair(playerOutId = "1", playerInId = "4")
    }
}

class FakeTimeTicker : TimeTicker {
    private val _flow = MutableSharedFlow<Long>(replay = 1)
    override val timeFlow: Flow<Long> = _flow

    suspend fun emit(time: Long) = _flow.emit(time)
}
