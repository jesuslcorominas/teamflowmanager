package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPreviousCaptainsUseCase
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private lateinit var createMatch: CreateMatchUseCase
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var updateMatchUseCase: UpdateMatchUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: MatchCreationWizardViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testPlayers = listOf(
        Player(1L, "John", "Doe", 1, listOf(Position.Goalkeeper), 1L, false),
        Player(2L, "Jane", "Smith", 2, listOf(Position.Defender), 1L, false),
        Player(3L, "Bob", "Johnson", 3, listOf(Position.Midfielder), 1L, false),
        Player(4L, "Alice", "Brown", 4, listOf(Position.Forward), 1L, false),
        Player(5L, "Charlie", "Wilson", 5, listOf(Position.Defender), 1L, false),
        Player(6L, "David", "Lee", 6, listOf(Position.Midfielder), 1L, false),
    )

    private val testTeam = Team(1L, "Test Team", "Coach", "Delegate", null, TeamType.FOOTBALL_5)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        getPreviousCaptainsUseCase = mockk()
        getDefaultCaptainUseCase = mockk()
        saveDefaultCaptainUseCase = mockk()
        getCaptainPlayerUseCase = mockk()
        getTeamUseCase = mockk()
        createMatch = mockk()
        getMatchByIdUseCase = mockk()
        updateMatchUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        savedStateHandle = mockk()

        every { getPlayersUseCase.invoke() } returns flowOf(testPlayers)
        every { getTeamUseCase.invoke() } returns flowOf(testTeam)
        every { savedStateHandle.get<Long>(Route.CreateMatch.ARG_MATCH_ID) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MatchCreationWizardViewModel {
        return MatchCreationWizardViewModel(
            getPlayersUseCase = getPlayersUseCase,
            getPreviousCaptainsUseCase = getPreviousCaptainsUseCase,
            getDefaultCaptainUseCase = getDefaultCaptainUseCase,
            saveDefaultCaptainUseCase = saveDefaultCaptainUseCase,
            getCaptainPlayerUseCase = getCaptainPlayerUseCase,
            getTeamUseCase = getTeamUseCase,
            createMatch = createMatch,
            getMatchByIdUseCase = getMatchByIdUseCase,
            updateMatchUseCase = updateMatchUseCase,
            analyticsTracker = analyticsTracker,
            crashReporter = crashReporter,
            savedStateHandle = savedStateHandle
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
    fun `initial step should be GENERAL_DATA`() = runTest {
        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(WizardStep.GENERAL_DATA, viewModel.currentStep.value)
    }

    @Test
    fun `goToNextStep should advance through steps`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
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
    fun `goToPreviousStep should go back through steps`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { getCaptainPlayerUseCase.invoke() } returns null
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
    fun `hasGoalkeepersInSquad should return true when goalkeeper is selected`() = runTest {
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
    fun `hasGoalkeepersInSquad should return false when no goalkeeper is selected`() = runTest {
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
    fun `checkIfShouldAskForDefaultCaptain should return true when captain was in last 2 matches`() = runTest {
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
    fun `checkIfShouldAskForDefaultCaptain should return false when captain was not in last 2 matches`() = runTest {
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
    fun `setDefaultCaptain should call saveDefaultCaptainUseCase`() = runTest {
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
    fun `buildMatch should create match with correct data`() = runTest {
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
        assertEquals(3601000L, match.dateTime)
        assertEquals(6, match.squadCallUpIds.size)
        assertEquals(2L, match.captainId)
        assertEquals(5, match.startingLineupIds.size)
    }

    @Test
    fun `setGeneralData should accept 00-00 time correctly`() = runTest {
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
    fun `buildMatch should create match with 00-00 time`() = runTest {
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
        assertEquals(1000L, match.dateTime)
        assertEquals(6, match.squadCallUpIds.size)
        assertEquals(2L, match.captainId)
        assertEquals(5, match.startingLineupIds.size)
    }
}
