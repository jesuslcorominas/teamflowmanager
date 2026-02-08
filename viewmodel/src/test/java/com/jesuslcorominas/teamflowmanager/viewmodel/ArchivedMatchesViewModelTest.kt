package com.jesuslcorominas.teamflowmanager.viewmodel
import com.jesuslcorominas.teamflowmanager.domain.model.*

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UnarchiveMatchUseCase
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

@ExperimentalCoroutinesApi
class ArchivedMatchesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getArchivedMatchesUseCase: GetArchivedMatchesUseCase
    private lateinit var unarchiveMatchUseCase: UnarchiveMatchUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: ArchivedMatchesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getArchivedMatchesUseCase = mockk()
        unarchiveMatchUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getArchivedMatchesUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel =
            ArchivedMatchesViewModel(
                getArchivedMatchesUseCase,
                unarchiveMatchUseCase,
                analyticsTracker,
                crashReporter,
            )

        // Then
        assertEquals(ArchivedMatchesUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `should emit Empty state when no archived matches available`() =
        runTest {
            // Given
            every { getArchivedMatchesUseCase.invoke() } returns flowOf(emptyList())

            // When
            viewModel =
                ArchivedMatchesViewModel(
                    getArchivedMatchesUseCase,
                    unarchiveMatchUseCase,
                    analyticsTracker,
                    crashReporter,
                )
            advanceUntilIdle()

            // Then
            assertEquals(ArchivedMatchesUiState.Empty, viewModel.uiState.value)
        }

    @Test
    fun `should emit Success state with archived matches when available`() =
        runTest {
            // Given
            val archivedMatches =
                listOf(
                    Match(
                        id = 1L,
                        opponent = "Team A",
                        location = "Location",
                        periodType = PeriodType.HALF_TIME,
                        captainId = 1L,
                        status = MatchStatus.FINISHED,
                        teamName = "Home Team",
                        archived = true,
                    ),
                    Match(
                        id = 2L,
                        opponent = "Team B",
                        location = "Location",
                        periodType = PeriodType.HALF_TIME,
                        captainId = 1L,
                        status = MatchStatus.FINISHED,
                        teamName = "Home Team",
                        archived = true,
                    ),
                )
            every { getArchivedMatchesUseCase.invoke() } returns flowOf(archivedMatches)

            // When
            viewModel =
                ArchivedMatchesViewModel(
                    getArchivedMatchesUseCase,
                    unarchiveMatchUseCase,
                    analyticsTracker,
                    crashReporter,
                )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArchivedMatchesUiState.Success)
            assertEquals(archivedMatches, (state as ArchivedMatchesUiState.Success).matches)
        }

    @Test
    fun `unarchiveMatch should call unarchiveMatchUseCase`() =
        runTest {
            // Given
            every { getArchivedMatchesUseCase.invoke() } returns flowOf(emptyList())
            viewModel =
                ArchivedMatchesViewModel(
                    getArchivedMatchesUseCase,
                    unarchiveMatchUseCase,
                    analyticsTracker,
                    crashReporter,
                )
            val matchId = 1L

            // When
            viewModel.unarchiveMatch(matchId)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { unarchiveMatchUseCase.invoke(matchId) }
        }
}
