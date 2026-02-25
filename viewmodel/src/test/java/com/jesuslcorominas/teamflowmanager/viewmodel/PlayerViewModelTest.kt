package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateScheduledMatchesCaptainUseCase
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

@ExperimentalCoroutinesApi
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var addPlayerUseCase: AddPlayerUseCase
    private lateinit var deletePlayerUseCase: DeletePlayerUseCase
    private lateinit var updatePlayerUseCase: UpdatePlayerUseCase
    private lateinit var getCaptainPlayerUseCase: GetCaptainPlayerUseCase
    private lateinit var updateScheduledMatchesCaptainUseCase: UpdateScheduledMatchesCaptainUseCase
    private lateinit var setPlayerAsCaptainUseCase: SetPlayerAsCaptainUseCase
    private lateinit var removePlayerAsCaptainUseCase: RemovePlayerAsCaptainUseCase
    private lateinit var getScheduledMatchesUseCase: GetScheduledMatchesUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        addPlayerUseCase = mockk(relaxed = true)
        deletePlayerUseCase = mockk()
        updatePlayerUseCase = mockk()
        getCaptainPlayerUseCase = mockk()
        updateScheduledMatchesCaptainUseCase = mockk(relaxed = true)
        setPlayerAsCaptainUseCase = mockk(relaxed = true)
        removePlayerAsCaptainUseCase = mockk(relaxed = true)
        getScheduledMatchesUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = PlayerViewModel(
        getPlayersUseCase = getPlayersUseCase,
        addPlayerUseCase = addPlayerUseCase,
        updatePlayerUseCase = updatePlayerUseCase,
        deletePlayerUseCase = deletePlayerUseCase,
        getCaptainPlayerUseCase = getCaptainPlayerUseCase,
        updateScheduledMatchesCaptainUseCase = updateScheduledMatchesCaptainUseCase,
        setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
        removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
        getScheduledMatchesUseCase = getScheduledMatchesUseCase,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
    )

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel = createViewModel()

        // Then
        assertEquals(PlayerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when players are loaded`() = runTest(testDispatcher) {
        // Given
        val players = listOf(
            Player(1L, "John", "Doe", 10, listOf(Position.Forward), teamId = 1L, isCaptain = false),
            Player(2L, "Jane", "Smith", 8, listOf(Position.Midfielder), teamId = 1L, isCaptain = false),
        )
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = createViewModel()
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
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(PlayerUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `addPlayer should call addPlayerUseCase`() = runTest(testDispatcher) {
        // Given
        val player = Player(
            id = 0L,
            firstName = "John",
            lastName = "Doe",
            number = 2,
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false,
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
        viewModel = createViewModel()

        // When
        viewModel.addPlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { addPlayerUseCase.invoke(player) }
    }

    @Test
    fun `showDeleteConfirmation should update deleteConfirmationState`() = runTest(testDispatcher) {
        // Given
        val player = Player(1L, "John", "Doe", 10, listOf(Position.Forward), teamId = 1L, isCaptain = false)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = createViewModel()

        // When
        viewModel.showDeleteConfirmation(player)

        // Then
        assertEquals(DeleteConfirmationState.Confirming(player), viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `dismissDeleteConfirmation should reset deleteConfirmationState`() = runTest(testDispatcher) {
        // Given
        val player = Player(1L, "John", "Doe", 10, listOf(Position.Forward), teamId = 1L, isCaptain = false)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = createViewModel()
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
        viewModel = createViewModel()

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
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            number = 2,
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false,
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
        coEvery { updatePlayerUseCase.invoke(player) } just runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updatePlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { updatePlayerUseCase.invoke(player) }
    }
}
