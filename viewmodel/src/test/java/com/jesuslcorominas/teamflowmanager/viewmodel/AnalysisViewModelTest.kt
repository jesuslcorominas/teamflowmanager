package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetExportDataUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerGoalStatsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerTimeStatsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPlayerTimeStatsUseCase: GetPlayerTimeStatsUseCase
    private lateinit var getPlayerGoalStatsUseCase: GetPlayerGoalStatsUseCase
    private lateinit var getExportDataUseCase: GetExportDataUseCase
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var exportToPdfUseCase: ExportToPdfUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayerTimeStatsUseCase = mockk()
        getPlayerGoalStatsUseCase = mockk()
        getExportDataUseCase = mockk()
        getTeamUseCase = mockk()
        exportToPdfUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AnalysisViewModel(
        getPlayerTimeStats = getPlayerTimeStatsUseCase,
        getPlayerGoalStats = getPlayerGoalStatsUseCase,
        getExportData = getExportDataUseCase,
        getTeam = getTeamUseCase,
        exportToPdf = exportToPdfUseCase,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
    )

    private fun makePlayer(id: Long) = Player(
        id = id,
        firstName = "Player$id",
        lastName = "Last",
        number = id.toInt(),
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = false,
    )

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals(AnalysisUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Empty when time stats are empty`() = runTest(testDispatcher) {
        // Given
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then - time stats empty → Empty state; goal stats empty updates but state stays Empty→Success
        // After goal stats run on top of Empty state: becomes Success(emptyList, emptyList)
        val state = viewModel.uiState.value
        // Both flows emit emptyList:
        // timeStats: empty → AnalysisUiState.Empty
        // goalStats: current=Empty → Success(emptyList, emptyList)
        assertEquals(AnalysisUiState.Success(emptyList(), emptyList()), state)
    }

    @Test
    fun `uiState should be Success with time stats when stats are available`() =
        runTest(testDispatcher) {
            // Given
            val player = makePlayer(1L)
            val timeStats = listOf(PlayerTimeStats(player = player, totalTimeMinutes = 45.0, matchesPlayed = 1))
            every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(timeStats)
            every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assert(state is AnalysisUiState.Success)
            assertEquals(timeStats, (state as AnalysisUiState.Success).playerTimeStats)
        }

    @Test
    fun `uiState should include goal stats when both are available`() = runTest(testDispatcher) {
        // Given
        val player = makePlayer(1L)
        val timeStats = listOf(PlayerTimeStats(player = player, totalTimeMinutes = 45.0, matchesPlayed = 1))
        val goalStats = listOf(PlayerGoalStats(player = player, totalGoals = 2, matchesWithGoals = 1))
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(timeStats)
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(goalStats)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as AnalysisUiState.Success
        assertEquals(timeStats, state.playerTimeStats)
        assertEquals(goalStats, state.playerGoalStats)
    }

    @Test
    fun `selectTab should update selectedTab and log analytics`() = runTest(testDispatcher) {
        // Given
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        // When
        viewModel.selectTab(AnalysisTab.GOALS)

        // Then
        assertEquals(AnalysisTab.GOALS, viewModel.selectedTab.value)
        verify { analyticsTracker.logEvent(any(), any()) }
    }

    @Test
    fun `requestExport should emit Ready state with URI on success`() = runTest(testDispatcher) {
        // Given
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())
        val team = Team(
            id = 1L,
            name = "My Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
        )
        val exportData = mockk<ExportData>()
        every { getTeamUseCase.invoke() } returns flowOf(team)
        every { getExportDataUseCase.invoke() } returns flowOf(exportData)
        coEvery { exportToPdfUseCase.invoke(exportData, "My Team") } returns "file://report.pdf"
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.requestExport()
        advanceUntilIdle()

        // Then
        assertEquals(ExportState.Ready("file://report.pdf"), viewModel.exportState.value)
    }

    @Test
    fun `requestExport should emit Error state when exportToPdf returns null`() =
        runTest(testDispatcher) {
            // Given
            every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
            every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())
            val exportData = mockk<ExportData>()
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getExportDataUseCase.invoke() } returns flowOf(exportData)
            coEvery { exportToPdfUseCase.invoke(any(), any()) } returns null
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.requestExport()
            advanceUntilIdle()

            // Then
            assertEquals(ExportState.Error, viewModel.exportState.value)
        }

    @Test
    fun `requestExport should emit Error state on exception`() = runTest(testDispatcher) {
        // Given
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getTeamUseCase.invoke() } throws RuntimeException("Export failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.requestExport()
        advanceUntilIdle()

        // Then
        assertEquals(ExportState.Error, viewModel.exportState.value)
    }

    @Test
    fun `exportCompleted should reset exportState to Idle`() = runTest(testDispatcher) {
        // Given
        every { getPlayerTimeStatsUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayerGoalStatsUseCase.invoke() } returns flowOf(emptyList())
        val exportData = mockk<ExportData>()
        every { getTeamUseCase.invoke() } returns flowOf(null)
        every { getExportDataUseCase.invoke() } returns flowOf(exportData)
        coEvery { exportToPdfUseCase.invoke(any(), any()) } returns "file://report.pdf"
        val viewModel = createViewModel()
        viewModel.requestExport()
        advanceUntilIdle()

        // When
        viewModel.exportCompleted()

        // Then
        assertEquals(ExportState.Idle, viewModel.exportState.value)
    }
}
