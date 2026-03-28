package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubByFirestoreIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateMatchUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchCreationWizardViewModelTest {
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var getPreviousCaptainsUseCase: GetPreviousCaptainsUseCase
    private lateinit var getDefaultCaptainUseCase: GetDefaultCaptainUseCase
    private lateinit var saveDefaultCaptainUseCase: SaveDefaultCaptainUseCase
    private lateinit var getCaptainPlayerUseCase: GetCaptainPlayerUseCase
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getClubByFirestoreIdUseCase: GetClubByFirestoreIdUseCase
    private lateinit var createMatchUseCase: CreateMatchUseCase
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var updateMatchUseCase: UpdateMatchUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter

    private lateinit var viewModel: MatchCreationWizardViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testPlayers = listOf(
        Player(1L, "John", "Doe", 1, listOf(Position.Goalkeeper), teamId = 1L, isCaptain = false),
        Player(2L, "Jane", "Smith", 2, listOf(Position.Defender), teamId = 1L, isCaptain = false),
        Player(3L, "Bob", "Johnson", 3, listOf(Position.Midfielder), teamId = 1L, isCaptain = false),
        Player(4L, "Alice", "Brown", 4, listOf(Position.Forward), teamId = 1L, isCaptain = false),
        Player(5L, "Charlie", "Wilson", 5, listOf(Position.Defender), teamId = 1L, isCaptain = false),
        Player(6L, "David", "Lee", 6, listOf(Position.Midfielder), teamId = 1L, isCaptain = false),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        getPreviousCaptainsUseCase = mockk()
        getDefaultCaptainUseCase = mockk()
        saveDefaultCaptainUseCase = mockk()
        getCaptainPlayerUseCase = mockk()
        getTeamUseCase = mockk()
        getClubByFirestoreIdUseCase = mockk(relaxed = true)
        createMatchUseCase = mockk(relaxed = true)
        getMatchByIdUseCase = mockk(relaxed = true)
        updateMatchUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)

        every { getPlayersUseCase.invoke() } returns flowOf(testPlayers)
        every { getTeamUseCase.invoke() } returns flowOf(null)
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(matchId: Long = 0L): MatchCreationWizardViewModel {
        return MatchCreationWizardViewModel(
            matchId = matchId,
            getPlayersUseCase = getPlayersUseCase,
            getPreviousCaptainsUseCase = getPreviousCaptainsUseCase,
            getDefaultCaptainUseCase = getDefaultCaptainUseCase,
            saveDefaultCaptainUseCase = saveDefaultCaptainUseCase,
            getCaptainPlayerUseCase = getCaptainPlayerUseCase,
            getTeamUseCase = getTeamUseCase,
            getClubByFirestoreIdUseCase = getClubByFirestoreIdUseCase,
            createMatch = createMatchUseCase,
            getMatchByIdUseCase = getMatchByIdUseCase,
            updateMatchUseCase = updateMatchUseCase,
            analyticsTracker = analyticsTracker,
            crashReporter = crashReporter,
        )
    }

    @Test
    fun `initial state should be loading then ready`() = runTest {
        // When
        viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            assertEquals(MatchCreationWizardUiState.Loading, awaitItem())
            val readyState = awaitItem()
            assertTrue(readyState is MatchCreationWizardUiState.Ready)
            assertEquals(testPlayers, (readyState as MatchCreationWizardUiState.Ready).players)
        }
    }

    @Test
    fun `initial step should be GENERAL_DATA`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(WizardStep.GENERAL_DATA, viewModel.currentStep.value)
    }

    @Test
    fun `goToNextStep should advance through steps`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When & Then
        assertEquals(WizardStep.GENERAL_DATA, viewModel.currentStep.value)

        viewModel.goToNextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.SQUAD_CALLUP, viewModel.currentStep.value)

        viewModel.goToNextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.CAPTAIN, viewModel.currentStep.value)

        viewModel.goToNextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.STARTING_LINEUP, viewModel.currentStep.value)
    }

    @Test
    fun `goToPreviousStep should go back through steps`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.goToNextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.goToNextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.goToNextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.STARTING_LINEUP, viewModel.currentStep.value)

        // When & Then
        viewModel.goToPreviousStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.CAPTAIN, viewModel.currentStep.value)

        viewModel.goToPreviousStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.SQUAD_CALLUP, viewModel.currentStep.value)

        viewModel.goToPreviousStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(WizardStep.GENERAL_DATA, viewModel.currentStep.value)
    }

    @Test
    fun `hasGoalkeepersInSquad should return true when goalkeeper is selected`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L, 4L, 5L)) // Player 1 is goalkeeper

        // When
        val result = viewModel.hasGoalkeepersInSquad()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasGoalkeepersInSquad should return false when no goalkeeper is selected`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(2L, 3L, 4L, 5L, 6L)) // No goalkeeper

        // When
        val result = viewModel.hasGoalkeepersInSquad()

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkIfShouldAskForDefaultCaptain should return true when captain was in last 2 matches`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setCaptain(2L)
        coEvery { getDefaultCaptainUseCase.invoke() } returns null
        coEvery { getPreviousCaptainsUseCase.invoke(2) } returns listOf(2L, 2L)

        // When
        val (shouldAsk, player) = viewModel.checkIfShouldAskForDefaultCaptain()

        // Then
        assertTrue(shouldAsk)
        assertEquals(2L, player?.id)
    }

    @Test
    fun `checkIfShouldAskForDefaultCaptain should return false when captain was not in last 2 matches`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setCaptain(2L)
        coEvery { getDefaultCaptainUseCase.invoke() } returns null
        coEvery { getPreviousCaptainsUseCase.invoke(2) } returns listOf(3L, 4L)

        // When
        val (shouldAsk, _) = viewModel.checkIfShouldAskForDefaultCaptain()

        // Then
        assertFalse(shouldAsk)
    }

    @Test
    fun `setDefaultCaptain should call saveDefaultCaptainUseCase`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        every { saveDefaultCaptainUseCase.invoke(2L) } just runs

        // When
        viewModel.setDefaultCaptain(2L)

        // Then
        coVerify { saveDefaultCaptainUseCase.invoke(2L) }
    }

    @Test
    fun `buildMatch should create match with correct data`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setGeneralData("Opponent", "Location", 1000L, 3600000L, 2)
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L, 4L, 5L, 6L))
        viewModel.setCaptain(2L)
        viewModel.setStartingLineup(setOf(1L, 2L, 3L, 4L, 5L))

        // When
        val match = viewModel.buildMatch()

        // Then
        assertEquals("Opponent", match.opponent)
        assertEquals("Location", match.location)
        assertEquals(1000L + 3600000L, match.dateTime)
        assertEquals(6, match.squadCallUpIds.size)
        assertEquals(2L, match.captainId)
        assertEquals(5, match.startingLineupIds.size)
    }

    @Test
    fun `setGeneralData should accept 00-00 time correctly`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Setting time to 00:00 (0L milliseconds from midnight)
        viewModel.setGeneralData("Opponent", "Location", 1000L, 0L, 2)

        // Then - Time should be 0L, not null
        assertEquals(0L, viewModel.getTime())
        assertEquals("Opponent", viewModel.getOpponent())
        assertEquals("Location", viewModel.getLocation())
        assertEquals(1000L, viewModel.getDate())
        assertEquals(2, viewModel.getNumberOfPeriods())
    }

    @Test
    fun `buildMatch should create match with 00-00 time`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setGeneralData("Opponent", "Location", 1000L, 0L, 2) // 00:00 time
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L, 4L, 5L, 6L))
        viewModel.setCaptain(2L)
        viewModel.setStartingLineup(setOf(1L, 2L, 3L, 4L, 5L))

        // When
        val match = viewModel.buildMatch()

        // Then
        assertEquals("Opponent", match.opponent)
        assertEquals("Location", match.location)
        assertEquals(1000L + 0L, match.dateTime) // dateTime = date + time = 1000L
        assertEquals(6, match.squadCallUpIds.size)
        assertEquals(2L, match.captainId)
        assertEquals(5, match.startingLineupIds.size)
    }

    // ── createMatch ──────────────────────────────────────────────────────────

    @Test
    fun `createMatch success should set Saving then call onComplete`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setGeneralData("Opponent", "Stadium", null, null, 2)
        val onComplete = mockk<() -> Unit>(relaxed = true)

        // When
        viewModel.createMatch(viewModel.buildMatch(), onComplete)
        advanceUntilIdle()

        // Then
        coVerify { createMatchUseCase.invoke(any()) }
        coVerify { onComplete() }
    }

    @Test
    fun `createMatch error should restore Ready state and call onComplete`() = runTest(testDispatcher) {
        // Given
        coEvery { createMatchUseCase.invoke(any()) } throws RuntimeException("network error")
        viewModel = createViewModel()
        advanceUntilIdle()
        val onComplete = mockk<() -> Unit>(relaxed = true)

        // When
        viewModel.createMatch(viewModel.buildMatch(), onComplete)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is MatchCreationWizardUiState.Ready)
        coVerify { onComplete() }
    }

    // ── updateMatch ───────────────────────────────────────────────────────────

    @Test
    fun `updateMatch success should update match and call onComplete`() = runTest(testDispatcher) {
        // Given
        val matchId = 42L
        val existingMatch = Match(
            id = matchId,
            teamName = "Team",
            opponent = "Old Opponent",
            location = "Old Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
        )
        coEvery { getMatchByIdUseCase.invoke(matchId) } returns flowOf(existingMatch)
        viewModel = createViewModel(matchId)
        advanceUntilIdle()
        val onComplete = mockk<() -> Unit>(relaxed = true)

        // When
        viewModel.updateMatch(onComplete)
        advanceUntilIdle()

        // Then
        coVerify { updateMatchUseCase.invoke(any()) }
        coVerify { onComplete() }
    }

    @Test
    fun `updateMatch error should restore Ready state and call onComplete`() = runTest(testDispatcher) {
        // Given
        val matchId = 42L
        val existingMatch = Match(
            id = matchId,
            teamName = "Team",
            opponent = "Old Opponent",
            location = "Old Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
        )
        coEvery { getMatchByIdUseCase.invoke(matchId) } returns flowOf(existingMatch)
        coEvery { updateMatchUseCase.invoke(any()) } throws RuntimeException("error")
        viewModel = createViewModel(matchId)
        advanceUntilIdle()
        val onComplete = mockk<() -> Unit>(relaxed = true)

        // When
        viewModel.updateMatch(onComplete)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is MatchCreationWizardUiState.Ready)
        coVerify { onComplete() }
    }

    // ── edit mode ─────────────────────────────────────────────────────────────

    @Test
    fun `edit mode should load match data and populate fields`() = runTest(testDispatcher) {
        // Given
        val matchId = 99L
        val match = Match(
            id = matchId,
            teamName = "Team",
            opponent = "FC Test",
            location = "Home Ground",
            dateTime = 86400000L + 3600000L,
            periodType = PeriodType.HALF_TIME,
            squadCallUpIds = listOf(1L, 2L, 3L),
            captainId = 2L,
            startingLineupIds = listOf(1L, 2L),
        )
        coEvery { getMatchByIdUseCase.invoke(matchId) } returns flowOf(match)

        // When
        viewModel = createViewModel(matchId)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isEditMode())
        assertEquals("FC Test", viewModel.getOpponent())
        assertEquals("Home Ground", viewModel.getLocation())
        assertEquals(2L, viewModel.getCaptainId())
        assertEquals(setOf(1L, 2L, 3L), viewModel.getSquadCallUpIds())
        assertEquals(setOf(1L, 2L), viewModel.getStartingLineupIds())
    }

    // ── hasUnsavedChanges ─────────────────────────────────────────────────────

    @Test
    fun `hasUnsavedChanges in create mode returns false when nothing entered`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.hasUnsavedChanges())
    }

    @Test
    fun `hasUnsavedChanges in create mode returns true when opponent set`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setGeneralData("Rival FC", "", null, null, 2)

        // Then
        assertTrue(viewModel.hasUnsavedChanges())
    }

    @Test
    fun `hasUnsavedChanges in edit mode returns false when nothing changed`() = runTest(testDispatcher) {
        // Given
        val matchId = 55L
        val match = Match(
            id = matchId,
            teamName = "Team",
            opponent = "Same",
            location = "Same",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
        )
        coEvery { getMatchByIdUseCase.invoke(matchId) } returns flowOf(match)
        viewModel = createViewModel(matchId)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.hasUnsavedChanges())
    }

    @Test
    fun `hasUnsavedChanges in edit mode returns true when opponent changed`() = runTest(testDispatcher) {
        // Given
        val matchId = 55L
        val match = Match(
            id = matchId,
            teamName = "Team",
            opponent = "Original",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
        )
        coEvery { getMatchByIdUseCase.invoke(matchId) } returns flowOf(match)
        viewModel = createViewModel(matchId)
        advanceUntilIdle()

        // When
        viewModel.setGeneralData("Changed", "Stadium", null, null, 2)

        // Then
        assertTrue(viewModel.hasUnsavedChanges())
    }

    // ── requestBack / exitDialog ──────────────────────────────────────────────

    @Test
    fun `requestBack with no unsaved changes calls onNavigateBack immediately`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        var navigated = false

        // When
        viewModel.requestBack { navigated = true }

        // Then
        assertTrue(navigated)
        assertFalse(viewModel.showExitDialog.value)
    }

    @Test
    fun `requestBack with unsaved changes shows exit dialog`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setGeneralData("Rival", "", null, null, 2)
        var navigated = false

        // When
        viewModel.requestBack { navigated = true }

        // Then
        assertFalse(navigated)
        assertTrue(viewModel.showExitDialog.value)
    }

    @Test
    fun `dismissExitDialog sets showExitDialog to false`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setGeneralData("Rival", "", null, null, 2)
        viewModel.requestBack {}

        // When
        viewModel.dismissExitDialog()

        // Then
        assertFalse(viewModel.showExitDialog.value)
    }

    @Test
    fun `discardChanges sets showExitDialog to false and navigates back`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setGeneralData("Rival", "", null, null, 2)
        viewModel.requestBack {}
        var navigated = false

        // When
        viewModel.discardChanges { navigated = true }

        // Then
        assertFalse(viewModel.showExitDialog.value)
        assertTrue(navigated)
    }

    // ── goToNextStep / goToPreviousStep with fixed captain ────────────────────

    @Test
    fun `goToNextStep skips CAPTAIN step when fixed captain is in squad`() = runTest(testDispatcher) {
        // Given
        val fixedCaptain = testPlayers[0] // player id = 1
        coEvery { getCaptainPlayerUseCase.invoke() } returns fixedCaptain
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L))

        // When - advance from GENERAL_DATA to SQUAD_CALLUP
        viewModel.goToNextStep()
        advanceUntilIdle()
        assertEquals(WizardStep.SQUAD_CALLUP, viewModel.currentStep.value)

        // Then - from SQUAD_CALLUP should skip to STARTING_LINEUP (fixed captain in squad)
        viewModel.goToNextStep()
        advanceUntilIdle()
        assertEquals(WizardStep.STARTING_LINEUP, viewModel.currentStep.value)
        assertEquals(1L, viewModel.getCaptainId())
    }

    @Test
    fun `goToPreviousStep from STARTING_LINEUP goes to SQUAD_CALLUP when fixed captain was auto-assigned`() =
        runTest(testDispatcher) {
            // Given
            val fixedCaptain = testPlayers[0] // player id = 1
            coEvery { getCaptainPlayerUseCase.invoke() } returns fixedCaptain
            viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.setSquadCallUp(setOf(1L, 2L, 3L))
            // Navigate to STARTING_LINEUP (skipping CAPTAIN)
            viewModel.goToNextStep()
            advanceUntilIdle()
            viewModel.goToNextStep()
            advanceUntilIdle()
            assertEquals(WizardStep.STARTING_LINEUP, viewModel.currentStep.value)

            // When
            viewModel.goToPreviousStep()
            advanceUntilIdle()

            // Then - should go back to SQUAD_CALLUP, not CAPTAIN
            assertEquals(WizardStep.SQUAD_CALLUP, viewModel.currentStep.value)
        }

    // ── loadDefaultCaptainIfExists ────────────────────────────────────────────

    @Test
    fun `loadDefaultCaptainIfExists sets captain when default is in squad`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L))
        coEvery { getDefaultCaptainUseCase.invoke() } returns 2L

        // When
        viewModel.loadDefaultCaptainIfExists()

        // Then
        assertEquals(2L, viewModel.getCaptainId())
    }

    @Test
    fun `loadDefaultCaptainIfExists does not set captain when default is not in squad`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L))
        coEvery { getDefaultCaptainUseCase.invoke() } returns 99L // not in squad

        // When
        viewModel.loadDefaultCaptainIfExists()

        // Then
        assertEquals(0L, viewModel.getCaptainId())
    }

    // ── checkIfShouldAskForDefaultCaptain ─────────────────────────────────────

    @Test
    fun `checkIfShouldAskForDefaultCaptain returns false when default already set`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        coEvery { getDefaultCaptainUseCase.invoke() } returns 1L // already has default

        // When
        val (shouldAsk, _) = viewModel.checkIfShouldAskForDefaultCaptain()

        // Then
        assertFalse(shouldAsk)
    }

    // ── setSquadCallUp edge cases ─────────────────────────────────────────────

    @Test
    fun `setSquadCallUp clears captain when captain is removed from squad`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L))
        viewModel.setCaptain(2L)
        assertEquals(2L, viewModel.getCaptainId())

        // When - remove player 2 from squad
        viewModel.setSquadCallUp(setOf(1L, 3L))

        // Then
        assertEquals(0L, viewModel.getCaptainId())
    }

    @Test
    fun `setSquadCallUp removes player from starting lineup when removed from squad`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setSquadCallUp(setOf(1L, 2L, 3L, 4L, 5L))
        viewModel.setStartingLineup(setOf(1L, 2L, 3L, 4L, 5L))

        // When
        viewModel.setSquadCallUp(setOf(1L, 3L, 5L))

        // Then
        assertEquals(setOf(1L, 3L, 5L), viewModel.getStartingLineupIds())
    }

    // ── getters ───────────────────────────────────────────────────────────────

    @Test
    fun `getTeamTypePlayerCount returns team type value`() = runTest(testDispatcher) {
        // Given - team is null, default is 5
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(5, viewModel.getTeamTypePlayerCount())
    }

    @Test
    fun `isEditMode returns false in create mode`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEditMode())
    }

    @Test
    fun `buildMatch with null date and time produces null dateTime`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setGeneralData("Rival", "Pitch", null, null, 4)

        // When
        val match = viewModel.buildMatch()

        // Then
        assertNull(match.dateTime)
        assertEquals(4, match.numberOfPeriods)
    }
}
