package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import java.util.Date

@ExperimentalCoroutinesApi
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var addPlayerUseCase: AddPlayerUseCase
    private lateinit var deletePlayerUseCase: DeletePlayerUseCase
    private lateinit var updatePlayerUseCase: UpdatePlayerUseCase
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        addPlayerUseCase = mockk(relaxed = true)
        deletePlayerUseCase = mockk()
        updatePlayerUseCase = mockk()
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
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)

        // Then
        assertEquals(PlayerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when players are loaded`() = runTest(testDispatcher) {
        // Given
        val players = listOf(
            Player(1, "John", "Doe", null, listOf(Position.Forward)),
            Player(2, "Jane", "Smith", null, listOf(Position.Midfielder))
        )
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)
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
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)
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
            number = 2,
            positions = listOf(Position.Forward)
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)

        // When
        viewModel.addPlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { addPlayerUseCase.invoke(player) }
    }

    @Test
    fun `showDeleteConfirmation should update deleteConfirmationState`() = runTest(testDispatcher) {
        // Given
        val player = Player(1, "John", "Doe",2, listOf(Position.Forward))
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)

        // When
        viewModel.showDeleteConfirmation(player)

        // Then
        assertEquals(DeleteConfirmationState.Confirming(player), viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `dismissDeleteConfirmation should reset deleteConfirmationState`() = runTest(testDispatcher) {
        // Given
        val player = Player(1, "John", "Doe", 2,listOf(Position.Forward))
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)
        viewModel.showDeleteConfirmation(player)

        // When
        viewModel.dismissDeleteConfirmation()

        // Then
        assertEquals(DeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `deletePlayer should call deletePlayerUseCase and reset confirmation state`() = runTest(testDispatcher) {
        // Given
        val playerId = 1L
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        coEvery { deletePlayerUseCase(playerId) } just runs
        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)

        // When
        viewModel.deletePlayer(playerId)
        advanceUntilIdle()

        // Then
        coVerify { deletePlayerUseCase.invoke(playerId) }
        assertEquals(DeleteConfirmationState.None, viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `updatePlayer should call updatePlayerUseCase`() = runTest(testDispatcher) {
        // Given
        val player = Player(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            number = 2,
            positions = listOf(Position.Forward)
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        coEvery { updatePlayerUseCase.invoke(player) } just runs

        viewModel = PlayerViewModel(getPlayersUseCase, addPlayerUseCase, deletePlayerUseCase, updatePlayerUseCase)
        advanceUntilIdle()

        // When
        viewModel.updatePlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { updatePlayerUseCase.invoke(player) }
    }
}
