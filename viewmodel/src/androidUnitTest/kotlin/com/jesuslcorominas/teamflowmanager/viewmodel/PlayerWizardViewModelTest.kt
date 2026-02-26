package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateScheduledMatchesCaptainUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerWizardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPlayerByIdUseCase: GetPlayerByIdUseCase
    private lateinit var addPlayerUseCase: AddPlayerUseCase
    private lateinit var updatePlayerUseCase: UpdatePlayerUseCase
    private lateinit var getCaptainPlayerUseCase: GetCaptainPlayerUseCase
    private lateinit var updateScheduledMatchesCaptainUseCase: UpdateScheduledMatchesCaptainUseCase
    private lateinit var setPlayerAsCaptainUseCase: SetPlayerAsCaptainUseCase
    private lateinit var removePlayerAsCaptainUseCase: RemovePlayerAsCaptainUseCase
    private lateinit var getScheduledMatchesUseCase: GetScheduledMatchesUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayerByIdUseCase = mockk()
        addPlayerUseCase = mockk(relaxed = true)
        updatePlayerUseCase = mockk(relaxed = true)
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

    private fun createViewModelForCreate() = PlayerWizardViewModel(
        playerId = 0L,
        getPlayerByIdUseCase = getPlayerByIdUseCase,
        addPlayerUseCase = addPlayerUseCase,
        updatePlayerUseCase = updatePlayerUseCase,
        getCaptainPlayerUseCase = getCaptainPlayerUseCase,
        updateScheduledMatchesCaptainUseCase = updateScheduledMatchesCaptainUseCase,
        setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
        removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
        getScheduledMatchesUseCase = getScheduledMatchesUseCase,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
    )

    private fun createViewModelForEdit(playerId: Long) = PlayerWizardViewModel(
        playerId = playerId,
        getPlayerByIdUseCase = getPlayerByIdUseCase,
        addPlayerUseCase = addPlayerUseCase,
        updatePlayerUseCase = updatePlayerUseCase,
        getCaptainPlayerUseCase = getCaptainPlayerUseCase,
        updateScheduledMatchesCaptainUseCase = updateScheduledMatchesCaptainUseCase,
        setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
        removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
        getScheduledMatchesUseCase = getScheduledMatchesUseCase,
        analyticsTracker = analyticsTracker,
        crashReporter = crashReporter,
    )

    private fun makePlayer(
        id: Long = 1L,
        firstName: String = "John",
        lastName: String = "Doe",
        number: Int = 10,
        isCaptain: Boolean = false,
    ) = Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = isCaptain,
    )

    // --- Initialization tests ---

    @Test
    fun `initial state should be Ready for new player (create mode)`() = runTest(testDispatcher) {
        // When
        val viewModel = createViewModelForCreate()
        advanceUntilIdle()

        // Then
        assertEquals(PlayerWizardUiState.Ready, viewModel.uiState.value)
        assertFalse(viewModel.isEditMode())
    }

    @Test
    fun `initial state should be Ready for existing player (edit mode)`() = runTest(testDispatcher) {
        // Given
        val player = makePlayer(id = 5L)
        coEvery { getPlayerByIdUseCase.invoke(5L) } returns player

        // When
        val viewModel = createViewModelForEdit(5L)
        advanceUntilIdle()

        // Then
        assertEquals(PlayerWizardUiState.Ready, viewModel.uiState.value)
        assertTrue(viewModel.isEditMode())
        assertEquals("John", viewModel.getFirstName())
        assertEquals("Doe", viewModel.getLastName())
        assertEquals("10", viewModel.getNumber())
    }

    @Test
    fun `initial state should be Error when player not found`() = runTest(testDispatcher) {
        // Given
        coEvery { getPlayerByIdUseCase.invoke(99L) } returns null

        // When
        val viewModel = createViewModelForEdit(99L)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(state is PlayerWizardUiState.Error)
        assertEquals("Player not found", (state as PlayerWizardUiState.Error).message)
    }

    // --- Player data management ---

    @Test
    fun `setPlayerData should update player data fields`() {
        // When
        val viewModel = createViewModelForCreate()
        viewModel.setPlayerData("Alice", "Smith", "7", true, "uri://photo")

        // Then
        assertEquals("Alice", viewModel.getFirstName())
        assertEquals("Smith", viewModel.getLastName())
        assertEquals("7", viewModel.getNumber())
        assertTrue(viewModel.getIsCaptain())
        assertEquals("uri://photo", viewModel.getImageUri())
    }

    @Test
    fun `setPositions should update selected positions`() {
        // When
        val viewModel = createViewModelForCreate()
        val positions = listOf(Position.Goalkeeper, Position.Defender)
        viewModel.setPositions(positions)

        // Then
        assertEquals(positions, viewModel.getSelectedPositions())
    }

    // --- Change detection ---

    @Test
    fun `hasUnsavedChanges should return false initially for create mode`() {
        // When
        val viewModel = createViewModelForCreate()

        // Then
        assertFalse(viewModel.hasUnsavedChanges())
    }

    @Test
    fun `hasUnsavedChanges should return true when first name changes`() {
        // Given
        val viewModel = createViewModelForCreate()

        // When
        viewModel.setPlayerData("Alice", "", "", false, null)

        // Then
        assertTrue(viewModel.hasUnsavedChanges())
    }

    // --- Navigation / dialog ---

    @Test
    fun `requestBack without changes should call onNavigateBack`() {
        // Given
        val viewModel = createViewModelForCreate()
        var navigatedBack = false

        // When
        viewModel.requestBack { navigatedBack = true }

        // Then
        assertTrue(navigatedBack)
        assertFalse(viewModel.showExitDialog.value)
    }

    @Test
    fun `requestBack with unsaved changes should show exit dialog`() {
        // Given
        val viewModel = createViewModelForCreate()
        viewModel.setPlayerData("Alice", "", "", false, null)
        var navigatedBack = false

        // When
        viewModel.requestBack { navigatedBack = true }

        // Then
        assertFalse(navigatedBack)
        assertTrue(viewModel.showExitDialog.value)
    }

    @Test
    fun `dismissExitDialog should hide the dialog`() {
        // Given
        val viewModel = createViewModelForCreate()
        viewModel.setPlayerData("Alice", "", "", false, null)
        viewModel.requestBack { }

        // When
        viewModel.dismissExitDialog()

        // Then
        assertFalse(viewModel.showExitDialog.value)
    }

    @Test
    fun `discardChanges should hide dialog and navigate back`() {
        // Given
        val viewModel = createViewModelForCreate()
        viewModel.setPlayerData("Alice", "", "", false, null)
        viewModel.requestBack { }
        var navigatedBack = false

        // When
        viewModel.discardChanges { navigatedBack = true }

        // Then
        assertTrue(navigatedBack)
        assertFalse(viewModel.showExitDialog.value)
    }

    // --- Step navigation ---

    @Test
    fun `goToNextStep should advance from PLAYER_DATA to POSITIONS`() {
        // Given
        val viewModel = createViewModelForCreate()
        assertEquals(PlayerWizardStep.PLAYER_DATA, viewModel.currentStep.value)

        // When
        viewModel.goToNextStep()

        // Then
        assertEquals(PlayerWizardStep.POSITIONS, viewModel.currentStep.value)
    }

    @Test
    fun `goToPreviousStep should go back from POSITIONS to PLAYER_DATA`() {
        // Given
        val viewModel = createViewModelForCreate()
        viewModel.goToNextStep()
        assertEquals(PlayerWizardStep.POSITIONS, viewModel.currentStep.value)

        // When
        viewModel.goToPreviousStep()

        // Then
        assertEquals(PlayerWizardStep.PLAYER_DATA, viewModel.currentStep.value)
    }

    @Test
    fun `goToPreviousStep should stay on PLAYER_DATA when already on first step`() {
        // Given
        val viewModel = createViewModelForCreate()
        assertEquals(PlayerWizardStep.PLAYER_DATA, viewModel.currentStep.value)

        // When
        viewModel.goToPreviousStep()

        // Then
        assertEquals(PlayerWizardStep.PLAYER_DATA, viewModel.currentStep.value)
    }

    // --- Save player ---

    @Test
    fun `savePlayer should add new player when no captain conflict`() = runTest(testDispatcher) {
        // Given
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
        val viewModel = createViewModelForCreate()
        viewModel.setPlayerData("John", "Doe", "10", false, null)
        viewModel.setPositions(listOf(Position.Forward))
        var saved = false

        // When
        viewModel.savePlayer { saved = true }
        advanceUntilIdle()

        // Then
        assertTrue(saved)
        coVerify { addPlayerUseCase.invoke(any()) }
    }

    @Test
    fun `savePlayer should update existing player when in edit mode`() = runTest(testDispatcher) {
        // Given
        val player = makePlayer(id = 5L, firstName = "John")
        coEvery { getPlayerByIdUseCase.invoke(5L) } returns player
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
        val viewModel = createViewModelForEdit(5L)
        advanceUntilIdle()
        var saved = false

        // When
        viewModel.savePlayer { saved = true }
        advanceUntilIdle()

        // Then
        assertTrue(saved)
        coVerify { updatePlayerUseCase.invoke(any()) }
    }

    @Test
    fun `savePlayer should show ConfirmReplace when setting new captain and one already exists`() =
        runTest(testDispatcher) {
            // Given
            val existingCaptain = makePlayer(id = 2L, isCaptain = true)
            coEvery { getCaptainPlayerUseCase.invoke() } returns existingCaptain
            coEvery { getScheduledMatchesUseCase.invoke() } returns emptyList()
            val viewModel = createViewModelForCreate()
            viewModel.setPlayerData("Alice", "Smith", "7", true, null)
            viewModel.setPositions(listOf(Position.Forward))

            // When
            viewModel.savePlayer { }
            advanceUntilIdle()

            // Then
            val state = viewModel.captainConfirmationState.value
            assert(state is CaptainConfirmationState.ConfirmReplace)
        }

    @Test
    fun `savePlayer should show ConfirmReplaceWithMatches when captain exists and matches scheduled`() =
        runTest(testDispatcher) {
            // Given
            val existingCaptain = makePlayer(id = 2L, isCaptain = true)
            val scheduledMatch = mockk<Match>()
            coEvery { getCaptainPlayerUseCase.invoke() } returns existingCaptain
            coEvery { getScheduledMatchesUseCase.invoke() } returns listOf(scheduledMatch)
            val viewModel = createViewModelForCreate()
            viewModel.setPlayerData("Alice", "Smith", "7", true, null)
            viewModel.setPositions(listOf(Position.Forward))

            // When
            viewModel.savePlayer { }
            advanceUntilIdle()

            // Then
            val state = viewModel.captainConfirmationState.value
            assert(state is CaptainConfirmationState.ConfirmReplaceWithMatches)
            assertEquals(1, (state as CaptainConfirmationState.ConfirmReplaceWithMatches).matchCount)
        }

    @Test
    fun `savePlayer should show ConfirmRemove when removing captain from player with no matches`() =
        runTest(testDispatcher) {
            // Given
            val player = makePlayer(id = 5L, isCaptain = true)
            coEvery { getPlayerByIdUseCase.invoke(5L) } returns player
            coEvery { getCaptainPlayerUseCase.invoke() } returns player
            coEvery { getScheduledMatchesUseCase.invoke() } returns emptyList()
            val viewModel = createViewModelForEdit(5L)
            advanceUntilIdle()
            viewModel.setPlayerData("John", "Doe", "10", false, null)

            // When
            viewModel.savePlayer { }
            advanceUntilIdle()

            // Then
            val state = viewModel.captainConfirmationState.value
            assert(state is CaptainConfirmationState.ConfirmRemove)
        }

    @Test
    fun `confirmCaptainChange for ConfirmReplace should save player directly`() =
        runTest(testDispatcher) {
            // Given
            val existingCaptain = makePlayer(id = 2L, isCaptain = true)
            coEvery { getCaptainPlayerUseCase.invoke() } returns existingCaptain
            coEvery { getScheduledMatchesUseCase.invoke() } returns emptyList()
            val viewModel = createViewModelForCreate()
            viewModel.setPlayerData("Alice", "Smith", "7", true, null)
            viewModel.setPositions(listOf(Position.Forward))
            viewModel.savePlayer { }
            advanceUntilIdle()
            var saved = false

            // When
            viewModel.confirmCaptainChange(keepInMatches = false) { saved = true }
            advanceUntilIdle()

            // Then
            assertTrue(saved)
            assertEquals(CaptainConfirmationState.None, viewModel.captainConfirmationState.value)
        }

    @Test
    fun `cancelCaptainChange should reset captainConfirmationState to None`() =
        runTest(testDispatcher) {
            // Given
            val existingCaptain = makePlayer(id = 2L, isCaptain = true)
            coEvery { getCaptainPlayerUseCase.invoke() } returns existingCaptain
            coEvery { getScheduledMatchesUseCase.invoke() } returns emptyList()
            val viewModel = createViewModelForCreate()
            viewModel.setPlayerData("Alice", "Smith", "7", true, null)
            viewModel.setPositions(listOf(Position.Forward))
            viewModel.savePlayer { }
            advanceUntilIdle()

            // When
            viewModel.cancelCaptainChange()

            // Then
            assertEquals(CaptainConfirmationState.None, viewModel.captainConfirmationState.value)
        }

    @Test
    fun `savePlayer should call setPlayerAsCaptainUseCase when player is captain`() =
        runTest(testDispatcher) {
            // Given
            coEvery { getCaptainPlayerUseCase.invoke() } returns null
            val viewModel = createViewModelForCreate()
            viewModel.setPlayerData("Alice", "Smith", "7", true, null)
            viewModel.setPositions(listOf(Position.Forward))

            // When
            viewModel.savePlayer { }
            advanceUntilIdle()

            // Then
            coVerify { setPlayerAsCaptainUseCase.invoke(any()) }
        }

    @Test
    fun `savePlayer should call removePlayerAsCaptainUseCase when player was captain and is no longer`() =
        runTest(testDispatcher) {
            // Given - edit mode, player was captain, now setting isCaptain=false
            val player = makePlayer(id = 5L, isCaptain = false)
            coEvery { getPlayerByIdUseCase.invoke(5L) } returns player
            val captain = makePlayer(id = 5L, isCaptain = true)
            coEvery { getCaptainPlayerUseCase.invoke() } returns captain
            val viewModel = createViewModelForEdit(5L)
            advanceUntilIdle()
            // Player loaded with isCaptain=false, current captain is id=5
            // So player.id == captain.id, and !player.isCaptain — should call confirmRemove or directly remove

            // Since isCaptain=false and the loaded player.isCaptain is also false:
            // The ConfirmRemove flow only triggers when !player.isCaptain && currentCaptain.id == player.id
            // Since player.isCaptain is false (from loaded player data), this check applies
            coEvery { getScheduledMatchesUseCase.invoke() } returns emptyList()

            var saved = false
            viewModel.savePlayer { saved = true }
            advanceUntilIdle()

            // confirmRemove is shown
            viewModel.confirmCaptainChange(keepInMatches = false) { saved = true }
            advanceUntilIdle()

            assertTrue(saved)
            coVerify { removePlayerAsCaptainUseCase.invoke(any()) }
        }
}
