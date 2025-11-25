package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
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
    private lateinit var getArchivedMatchesUseCase: GetArchivedMatchesUseCase
    private lateinit var deleteMatchUseCase: DeleteMatchUseCase
    private lateinit var createMatchUseCase: CreateMatchUseCase
    private lateinit var updateMatchUseCase: UpdateMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
    private lateinit var archiveMatchUseCase: ArchiveMatchUseCase
    private lateinit var unarchiveMatchUseCase: UnarchiveMatchUseCase
    private lateinit var viewModel: MatchListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllMatchesUseCase = mockk()
        getArchivedMatchesUseCase = mockk()
        getMatchUseCase = mockk()
        deleteMatchUseCase = mockk(relaxed = true)
        createMatchUseCase = mockk(relaxed = true)
        updateMatchUseCase = mockk(relaxed = true)
        startMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
        archiveMatchUseCase = mockk(relaxed = true)
        unarchiveMatchUseCase = mockk(relaxed = true)
        setCurrentMatchUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
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
                setCurrentMatchUseCase,
                resumeMatchUseCase,
                archiveMatchUseCase,
                unarchiveMatchUseCase,
            )

        // Then
        assertEquals(MatchListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `should emit Empty state when no matches available`() =
        runTest {
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
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
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
                        date = System.currentTimeMillis(),
                    ),
                    Match(
                        id = 2L,
                        teamId = 1L,
                        opponent = "Team B",
                        teamName = "My Team",
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
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
                )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchListUiState.Success)
            assertEquals(matches, (state as MatchListUiState.Success).matches)
        }

    @Test
    fun `updateMatch should invoke updateMatchUseCase`() =
        runTest {
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
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "My Team",
                    opponent = "Updated Rival",
                    location = "Updated Stadium",
                    date = System.currentTimeMillis(),
                )

            // When
            viewModel.updateMatch(match)
            advanceUntilIdle()

            // Then
            coVerify { updateMatchUseCase.invoke(match) }
        }

    @Test
    fun `requestDeleteMatch should update deleteConfirmationState to Requested`() =
        runTest {
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
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "My Team",
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
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
            every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "My Team",
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
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
            every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
                )
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Rival FC",
                    teamName = "My Team",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
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
            every { getMatchUseCase.invoke() } returns flowOf(null)
            viewModel =
                MatchListViewModel(
                    getAllMatchesUseCase,
                    getMatchUseCase,
                    deleteMatchUseCase,
                    createMatchUseCase,
                    updateMatchUseCase,
                    startMatchUseCase,
                    setCurrentMatchUseCase,
                    resumeMatchUseCase,
                    archiveMatchUseCase,
                    unarchiveMatchUseCase,
                )
            val matchId = 1L

            // When
            viewModel.archiveMatch(matchId)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { archiveMatchUseCase.invoke(matchId) }
        }
}
