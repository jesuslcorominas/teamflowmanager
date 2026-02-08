package com.jesuslcorominas.teamflowmanager.viewmodel
import com.jesuslcorominas.teamflowmanager.domain.model.*

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
        updatePlayerUseCase = mockk(relaxed = true)
        getCaptainPlayerUseCase = mockk(relaxed = true)
        updateScheduledMatchesCaptainUseCase = mockk(relaxed = true)
        setPlayerAsCaptainUseCase = mockk(relaxed = true)
        removePlayerAsCaptainUseCase = mockk(relaxed = true)
        getScheduledMatchesUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)

        coEvery { getCaptainPlayerUseCase.invoke() } returns null
        coEvery { getScheduledMatchesUseCase.invoke() } returns emptyList()
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
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )

        // Then
        assertEquals(PlayerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when players are loaded`() = runTest(testDispatcher) {
        // Given
        val players = listOf(
            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1L, false),
            Player(2, "Jane", "Smith", 8, listOf(Position.Midfielder), 1L, false)
        )
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )
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
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )
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
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )

        // When
        viewModel.addPlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { addPlayerUseCase.invoke(player) }
    }

    @Test
    fun `showDeleteConfirmation should update deleteConfirmationState`() = runTest(testDispatcher) {
        // Given
        val player = Player(1, "John", "Doe", 10, listOf(Position.Forward), 1L, false)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )

        // When
        viewModel.showDeleteConfirmation(player)

        // Then
        assertEquals(DeleteConfirmationState.Confirming(player), viewModel.deleteConfirmationState.value)
    }

    @Test
    fun `dismissDeleteConfirmation should reset deleteConfirmationState`() = runTest(testDispatcher) {
        // Given
        val player = Player(1, "John", "Doe", 10, listOf(Position.Forward), 1L, false)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )
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
        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )

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
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false
        )
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
        coEvery { updatePlayerUseCase.invoke(player) } just runs

        viewModel = PlayerViewModel(
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
            crashReporter = crashReporter
        )
        advanceUntilIdle()

        // When
        viewModel.updatePlayer(player)
        advanceUntilIdle()

        // Then
        coVerify { updatePlayerUseCase.invoke(player) }
    }
}
