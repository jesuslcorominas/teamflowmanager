package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchReportDataUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
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
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelGoalTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var registerGoalUseCase: RegisterGoalUseCase
    private lateinit var shouldShowInvalidSubstitutionAlertUseCase: ShouldShowInvalidSubstitutionAlertUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter
    private lateinit var fakeTicker: FakeTimeTicker

    private val match = Match(
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchByIdUseCase = mockk()
        getAllPlayerTimesUseCase = mockk()
        getPlayersUseCase = mockk()
        registerGoalUseCase = mockk(relaxed = true)
        shouldShowInvalidSubstitutionAlertUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        fakeTicker = FakeTimeTicker()

        every { shouldShowInvalidSubstitutionAlertUseCase.invoke() } returns false

        every { getMatchByIdUseCase(MATCH_ID) } returns flowOf(match)
        every { getAllPlayerTimesUseCase(any()) } returns flowOf(emptyList())
        every { getPlayersUseCase() } returns flowOf(players)
    }

    private val getMatchTimelineUseCaseStub: GetMatchTimelineUseCase = mockk {
        every { this@mockk(any()) } returns flowOf(null)
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
        finishMatch = mockk(relaxed = true),
        pauseMatch = mockk(relaxed = true),
        resumeMatchUseCase = mockk(relaxed = true),
        startMatchTimerUseCase = mockk(relaxed = true),
        registerPlayerSubstitutionUseCase = mockk(relaxed = true),
        getMatchSummaryUseCase = mockk(relaxed = true),
        getMatchTimelineUseCase = getMatchTimelineUseCaseStub,
        registerGoal = registerGoalUseCase,
        startTimeoutUseCase = mockk(relaxed = true),
        endTimeoutUseCase = mockk(relaxed = true),
        getMatchReportData = mockk(relaxed = true),
        exportMatchReportToPdf = mockk(relaxed = true),
        synchronizeTimeUseCase = mockk(relaxed = true),
        startPlayerTimersBatchUseCase = mockk(relaxed = true),
        shouldShowInvalidSubstitutionAlertUseCase = shouldShowInvalidSubstitutionAlertUseCase,
        setShouldShowInvalidSubstitutionAlertUseCase = mockk(relaxed = true),
        timeTicker = fakeTicker,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
        notifyPresidentMatchEvent = mockk(relaxed = true),
        getTeamUseCase = mockk(relaxed = true),
        getPlayersByTeamUseCase = mockk(relaxed = true),
    )

    @Test
    fun `showGoalScorerDialog should set showGoalScorerDialog to true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.showGoalScorerDialog()
        advanceUntilIdle()
        assertTrue(viewModel.showGoalScorerDialog.value)
    }

    @Test
    fun `dismissGoalScorerDialog should set showGoalScorerDialog to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.showGoalScorerDialog()
        viewModel.dismissGoalScorerDialog()
        advanceUntilIdle()
        assertFalse(viewModel.showGoalScorerDialog.value)
    }

    @Test
    fun `registerGoal should call registerGoalUseCase and dismiss dialog`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.showGoalScorerDialog()

        viewModel.registerGoal(1L)
        advanceUntilIdle()

        coVerify { registerGoalUseCase(MATCH_ID, 1L, any(), false, false) }
        assertFalse(viewModel.showGoalScorerDialog.value)
    }

    @Test
    fun `showOpponentGoalDialog should set showOpponentGoalDialog to true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.showOpponentGoalDialog()
        advanceUntilIdle()
        assertTrue(viewModel.showOpponentGoalDialog.value)
    }

    @Test
    fun `dismissOpponentGoalDialog should set showOpponentGoalDialog to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.showOpponentGoalDialog()
        viewModel.dismissOpponentGoalDialog()
        advanceUntilIdle()
        assertFalse(viewModel.showOpponentGoalDialog.value)
    }

    @Test
    fun `registerOpponentGoal should call registerGoalUseCase with isOpponentGoal true and dismiss dialog`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.showOpponentGoalDialog()

        viewModel.registerOpponentGoal()
        advanceUntilIdle()

        coVerify { registerGoalUseCase(MATCH_ID, null, any(), true, false) }
        assertFalse(viewModel.showOpponentGoalDialog.value)
    }

    companion object {
        private const val MATCH_ID = 1L
    }
}
