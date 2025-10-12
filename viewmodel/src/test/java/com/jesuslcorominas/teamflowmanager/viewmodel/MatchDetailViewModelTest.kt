package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
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
class MatchDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var viewModel: MatchDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchByIdUseCase = mockk()
        getPlayersUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // Given/When
        viewModel = MatchDetailViewModel(getMatchByIdUseCase, getPlayersUseCase)

        // Then
        assertEquals(MatchDetailUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadMatch with null id should emit Create state with available players`() =
        runTest {
            // Given
            val players =
                listOf(
                    Player(1L, "John", "Doe", 10, listOf(Position.Forward)),
                    Player(2L, "Jane", "Smith", 7, listOf(Position.Midfielder)),
                )
            every { getPlayersUseCase.invoke() } returns flowOf(players)
            viewModel = MatchDetailViewModel(getMatchByIdUseCase, getPlayersUseCase)

            // When
            viewModel.loadMatch(null)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchDetailUiState.Create)
            assertEquals(players, (state as MatchDetailUiState.Create).availablePlayers)
        }

    @Test
    fun `loadMatch with valid id should emit Edit state with match and players`() =
        runTest {
            // Given
            val matchId = 1L
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
                    startingLineupIds = listOf(1L, 2L),
                    substituteIds = listOf(3L),
                )
            val players =
                listOf(
                    Player(1L, "John", "Doe", 10, listOf(Position.Forward)),
                    Player(2L, "Jane", "Smith", 7, listOf(Position.Midfielder)),
                    Player(3L, "Bob", "Johnson", 5, listOf(Position.Defender)),
                )
            every { getMatchByIdUseCase.invoke(matchId) } returns flowOf(match)
            every { getPlayersUseCase.invoke() } returns flowOf(players)
            viewModel = MatchDetailViewModel(getMatchByIdUseCase, getPlayersUseCase)

            // When
            viewModel.loadMatch(matchId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchDetailUiState.Edit)
            assertEquals(match, (state as MatchDetailUiState.Edit).match)
            assertEquals(players, state.availablePlayers)
        }

    @Test
    fun `loadMatch with invalid id should emit NotFound state`() =
        runTest {
            // Given
            val matchId = 999L
            val players =
                listOf(
                    Player(1L, "John", "Doe", 10, listOf(Position.Forward)),
                )
            every { getMatchByIdUseCase.invoke(matchId) } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(players)
            viewModel = MatchDetailViewModel(getMatchByIdUseCase, getPlayersUseCase)

            // When
            viewModel.loadMatch(matchId)
            advanceUntilIdle()

            // Then
            assertEquals(MatchDetailUiState.NotFound, viewModel.uiState.value)
        }
}
