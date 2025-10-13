package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class MatchListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAllMatchesUseCase: GetAllMatchesUseCase
    private lateinit var getMatchUseCase: GetMatchUseCase
    private lateinit var deleteMatchUseCase: DeleteMatchUseCase
    private lateinit var createMatchUseCase: CreateMatchUseCase
    private lateinit var updateMatchUseCase: UpdateMatchUseCase
    private lateinit var startMatchUseCase: StartMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
    private lateinit var viewModel: MatchListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllMatchesUseCase = mockk()
        getMatchUseCase = mockk()
        deleteMatchUseCase = mockk(relaxed = true)
        createMatchUseCase = mockk(relaxed = true)
        updateMatchUseCase = mockk(relaxed = true)
        startMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest(testDispatcher) {
        // Given
        every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)

        // When
        viewModel =
            MatchListViewModel(
                getAllMatchesUseCase,
                getMatchUseCase,
                deleteMatchUseCase,
                createMatchUseCase,
                updateMatchUseCase,
                startMatchUseCase,
                resumeMatchUseCase,
            )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchListUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit Empty state when no matches available`() =
        runTest(testDispatcher) {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)

            // When
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )

            // Then
            viewModel.uiState.test(timeout = 2.seconds) {
                assertEquals(MatchListUiState.Loading, awaitItem())
                assertEquals(MatchListUiState.Empty, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should emit Success state with matches when matches are available`() =
        runTest(testDispatcher) {
            // Given
            val matches =
                listOf(
                    Match(
                        id = 1L,
                        teamId = 1L,
                        opponent = "Rival FC",
                        location = "Stadium",
                        date = System.currentTimeMillis(),
                    ),
                    Match(
                        id = 2L,
                        teamId = 1L,
                        opponent = "Team B",
                        location = "Stadium 2",
                        date = System.currentTimeMillis(),
                    ),
                )
            every { getAllMatchesUseCase.invoke() } returns flowOf(matches)
            every { getMatchUseCase.invoke() } returns flowOf(null)

            // When
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )

            // Then
            viewModel.uiState.test(timeout = 2.seconds) {
                assertEquals(MatchListUiState.Loading, awaitItem())
                val state = awaitItem()
                assertTrue(state is MatchListUiState.Success)
                assertEquals(matches, (state as MatchListUiState.Success).matches)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `createMatch should invoke createMatchUseCase`() =
        runTest(testDispatcher) {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )
            val match =
                Match(
                    id = 0L,
                    teamId = 1L,
                    opponent = "New Rival",
                    location = "New Stadium",
                    date = System.currentTimeMillis(),
                )
            coEvery { createMatchUseCase.invoke(match) } returns 1L

            // When
            viewModel.createMatch(match)

            // Then
            coVerify { createMatchUseCase.invoke(match) }
        }

    @Test
    fun `updateMatch should invoke updateMatchUseCase`() =
        runTest(testDispatcher) {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Updated Rival",
                    location = "Updated Stadium",
                    date = System.currentTimeMillis(),
                )

            // When
            viewModel.updateMatch(match)

            // Then
            coVerify { updateMatchUseCase.invoke(match) }
        }

    @Test
    fun `requestDeleteMatch should update deleteConfirmationState to Requested`() =
        runTest(testDispatcher) {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
                )

            // When
            viewModel.requestDeleteMatch(match)

            // Then
            val state = viewModel.deleteConfirmationState.value
            assertTrue(state is MatchDeleteConfirmationState.Requested)
            assertEquals(match, (state as MatchDeleteConfirmationState.Requested).match)
        }

    @Test
    fun `confirmDeleteMatch should invoke deleteMatchUseCase and reset deleteConfirmationState`() =
        runTest(testDispatcher) {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
                )
            viewModel.requestDeleteMatch(match)

            // When
            viewModel.confirmDeleteMatch()

            // Then
            coVerify { deleteMatchUseCase.invoke(match.id) }
            assertEquals(MatchDeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
        }

    @Test
    fun `cancelDeleteMatch should reset deleteConfirmationState to None`() =
        runTest(testDispatcher) {
            // Given
            every { getAllMatchesUseCase.invoke() } returns flowOf(emptyList())
        every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    resumeMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
                )
            viewModel.requestDeleteMatch(match)

            // When
            viewModel.cancelDeleteMatch()

            // Then
            assertEquals(MatchDeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
        }
}
