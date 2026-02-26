package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAllMatchesUseCase: GetAllMatchesUseCase
    private lateinit var deleteMatchUseCase: DeleteMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
    private lateinit var archiveMatchUseCase: ArchiveMatchUseCase
    private lateinit var synchronizeTimeUseCase: SynchronizeTimeUseCase
    private lateinit var timeProvider: TimeProvider
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter

    private val testMatch = Match(
        id = 1L,
        teamId = 1L,
        teamName = "My Team",
        opponent = "Rival FC",
        location = "Stadium",
        periodType = PeriodType.HALF_TIME,
        captainId = 1L,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllMatchesUseCase = mockk()
        deleteMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
        archiveMatchUseCase = mockk(relaxed = true)
        synchronizeTimeUseCase = mockk(relaxed = true)
        timeProvider = mockk()
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        every { timeProvider.getCurrentTime() } returns System.currentTimeMillis()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MatchListViewModel(
        getAllMatchesUseCase = getAllMatchesUseCase,
        deleteMatchUseCase = deleteMatchUseCase,
        resumeMatchUseCase = resumeMatchUseCase,
        archiveMatchUseCase = archiveMatchUseCase,
        synchronizeTimeUseCase = synchronizeTimeUseCase,
        timeProvider = timeProvider,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
    )

    @Test
    fun `initial state should be Loading`() {
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        assertEquals(MatchListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Empty when no matches exist`() = runTest(testDispatcher) {
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(MatchListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success with matches when matches exist`() = runTest(testDispatcher) {
        val matches = listOf(testMatch)
        every { getAllMatchesUseCase() } returns flowOf(matches)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is MatchListUiState.Success)
        assertEquals(matches, (state as MatchListUiState.Success).matches)
    }

    @Test
    fun `requestDeleteMatch should update deleteConfirmationState to Requested`() = runTest(testDispatcher) {
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.requestDeleteMatch(testMatch)

        val state = viewModel.deleteConfirmationState.value
        assertTrue(state is MatchDeleteConfirmationState.Requested)
        assertEquals(testMatch, (state as MatchDeleteConfirmationState.Requested).match)
    }

    @Test
    fun `confirmDeleteMatch should call deleteMatchUseCase and reset deleteConfirmationState`() = runTest(testDispatcher) {
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.requestDeleteMatch(testMatch)

        viewModel.confirmDeleteMatch()
        advanceUntilIdle()

        coVerify { deleteMatchUseCase(testMatch.id) }
        assertEquals(MatchDeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `cancelDeleteMatch should reset deleteConfirmationState to None`() = runTest(testDispatcher) {
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        viewModel.requestDeleteMatch(testMatch)

        viewModel.cancelDeleteMatch()

        assertEquals(MatchDeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `archiveMatch should call archiveMatchUseCase`() = runTest(testDispatcher) {
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.archiveMatch(testMatch.id)
        advanceUntilIdle()

        coVerify { archiveMatchUseCase(testMatch.id) }
    }

    @Test
    fun `onQueryChange should filter matches by opponent name`() = runTest(testDispatcher) {
        val matches = listOf(
            testMatch,
            testMatch.copy(id = 2L, opponent = "Blue FC"),
        )
        every { getAllMatchesUseCase() } returns flowOf(matches)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("Rival")
        advanceUntilIdle()

        val state = viewModel.uiState.value as MatchListUiState.Success
        assertEquals(1, state.matches.size)
        assertEquals("Rival FC", state.matches[0].opponent)
    }
}
