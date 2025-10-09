package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
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
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var addPlayerUseCase: AddPlayerUseCase
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        addPlayerUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase)

        // Then
        assertEquals(PlayerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when players are loaded`() = runTest(testDispatcher) {
        // Given
        val players = listOf(
            Player(1, "John", "Doe", listOf(Position.Forward)),
            Player(2, "Jane", "Smith", listOf(Position.Midfielder))
        )
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(PlayerUiState.Success(players), state)
    }

    @Test
    fun `uiState should be Empty when no players exist`() = runTest(testDispatcher) {
        // Given
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(PlayerUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `addPlayer should call addPlayerUseCase`() = runTest(testDispatcher) {
        // Given
        val player = Player(
            id = 0,
            firstName = "John",
            lastName = "Doe",
            positions = listOf(Position.Forward)
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase)

        // When
        viewModel.addPlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { addPlayerUseCase.invoke(player) }
    }
}
