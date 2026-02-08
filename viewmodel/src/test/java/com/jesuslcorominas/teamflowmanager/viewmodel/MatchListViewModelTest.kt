package com.jesuslcorominas.teamflowmanager.viewmodel
import com.jesuslcorominas.teamflowmanager.domain.model.*

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
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
    private lateinit var viewModel: MatchListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllMatchesUseCase = mockk()
        deleteMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
        archiveMatchUseCase = mockk(relaxed = true)
        synchronizeTimeUseCase = mockk(relaxed = true)
        timeProvider = mockk(relaxed = true)
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
        every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel =
            MatchListViewModel(
                getAllMatchesUseCase,
                deleteMatchUseCase,
                resumeMatchUseCase,
                archiveMatchUseCase,
                synchronizeTimeUseCase,
                timeProvider,
                analyticsTracker,
                crashReporter
            )

        // Then
        assertEquals(MatchListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `should emit Empty state when no matches available`() =
        runTest {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())

            // When
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    deleteMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    synchronizeTimeUseCase,
                    timeProvider,
                    analyticsTracker,
                    crashReporter
                )
            advanceUntilIdle()

            // Then
            assertEquals(MatchListUiState.Empty, viewModel.uiState.value)
        }

    @Test
    fun `should emit Success state with matches when matches are available`() =
        runTest {
            // Given
            val matches =
                listOf(
                    Match(
                        id = 1L,
                        teamId = 1L,
                        opponent = "Rival FC",
                        teamName = "My Team",
                        location = "Stadium",
                        dateTime = System.currentTimeMillis(),
                        periodType = PeriodType.HALF_TIME,
                        captainId = 1L,
                    ),
                    Match(
                        id = 2L,
                        teamId = 1L,
                        opponent = "Team B",
                        teamName = "My Team",
                        location = "Stadium 2",
                        dateTime = System.currentTimeMillis(),
                        periodType = PeriodType.HALF_TIME,
                        captainId = 1L,
                    ),
                )
            every { getAllMatchesUseCase.invoke() } returns flowOf(matches)

            // When
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    deleteMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    synchronizeTimeUseCase,
                    timeProvider,
                    analyticsTracker,
                    crashReporter
                )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchListUiState.Success)
            assertEquals(matches, (state as MatchListUiState.Success).matches)
        }

    @Test
    fun `requestDeleteMatch should update deleteConfirmationState to Requested`() =
        runTest {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    deleteMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    synchronizeTimeUseCase,
                    timeProvider,
                    analyticsTracker,
                    crashReporter
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "My Team",
                    opponent = "Rival FC",
                    location = "Stadium",
                    dateTime = System.currentTimeMillis(),
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                )

            // When
            viewModel.requestDeleteMatch(match)
            advanceUntilIdle()

            // Then
            val state = viewModel.deleteConfirmationState.value
            assertTrue(state is MatchDeleteConfirmationState.Requested)
            assertEquals(match, (state as MatchDeleteConfirmationState.Requested).match)
        }

    @Test
    fun `confirmDeleteMatch should invoke deleteMatchUseCase and reset deleteConfirmationState`() =
        runTest {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    deleteMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    synchronizeTimeUseCase,
                    timeProvider,
                    analyticsTracker,
                    crashReporter
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "My Team",
                    opponent = "Rival FC",
                    location = "Stadium",
                    dateTime = System.currentTimeMillis(),
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                )
            viewModel.requestDeleteMatch(match)

            // When
            viewModel.confirmDeleteMatch()
            advanceUntilIdle()

            // Then
            coVerify { deleteMatchUseCase.invoke(match.id) }
            assertEquals(MatchDeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
        }

    @Test
    fun `cancelDeleteMatch should reset deleteConfirmationState to None`() =
        runTest {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    deleteMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    synchronizeTimeUseCase,
                    timeProvider,
                    analyticsTracker,
                    crashReporter
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Rival FC",
                    teamName = "My Team",
                    location = "Stadium",
                    dateTime = System.currentTimeMillis(),
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                )
            viewModel.requestDeleteMatch(match)

            // When
            viewModel.cancelDeleteMatch()

            // Then
            assertEquals(MatchDeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
        }

    @Test
    fun `archiveMatch should call archiveMatchUseCase`() =
        runTest {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    deleteMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    synchronizeTimeUseCase,
                    timeProvider,
                    analyticsTracker,
                    crashReporter
                )
            val matchId = 1L

            // When
            viewModel.archiveMatch(matchId)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { archiveMatchUseCase.invoke(matchId) }
        }
}
