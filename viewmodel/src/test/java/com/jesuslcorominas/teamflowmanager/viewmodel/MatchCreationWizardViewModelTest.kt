package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCase
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

    private lateinit var viewModel: MatchCreationWizardViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testPlayers = listOf(
        Player(1L, "John", "Doe", 1, listOf(Position.Goalkeeper)),
        Player(2L, "Jane", "Smith", 2, listOf(Position.Defender)),
        Player(3L, "Bob", "Johnson", 3, listOf(Position.Midfielder)),
        Player(4L, "Alice", "Brown", 4, listOf(Position.Forward)),
        Player(5L, "Charlie", "Wilson", 5, listOf(Position.Defender)),
        Player(6L, "David", "Lee", 6, listOf(Position.Midfielder)),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        getPreviousCaptainsUseCase = mockk()
        getDefaultCaptainUseCase = mockk()
        saveDefaultCaptainUseCase = mockk()

        every { getPlayersUseCase.invoke() } returns flowOf(testPlayers)
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
        testDispatcher.scheduler.advanceUntilIdle()

        // When & Then
        assertEquals(WizardStep.GENERAL_DATA, viewModel.currentStep.value)

        viewModel.goToNextStep()
        assertEquals(WizardStep.SQUAD_CALLUP, viewModel.currentStep.value)

        viewModel.goToNextStep()
        assertEquals(WizardStep.CAPTAIN, viewModel.currentStep.value)

        viewModel.goToNextStep()
        assertEquals(WizardStep.STARTING_LINEUP, viewModel.currentStep.value)
    }

    @Test
    fun `goToPreviousStep should go back through steps`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.goToNextStep()
        viewModel.goToNextStep()
        viewModel.goToNextStep()
        assertEquals(WizardStep.STARTING_LINEUP, viewModel.currentStep.value)

        // When & Then
        viewModel.goToPreviousStep()
        assertEquals(WizardStep.CAPTAIN, viewModel.currentStep.value)

        viewModel.goToPreviousStep()
        assertEquals(WizardStep.SQUAD_CALLUP, viewModel.currentStep.value)

        viewModel.goToPreviousStep()
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
        every { getDefaultCaptainUseCase.invoke() } returns null
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
        every { getDefaultCaptainUseCase.invoke() } returns null
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
        assertEquals(1000L, match.date)
        assertEquals(3600000L, match.time)
        assertEquals(6, match.squadCallUpIds.size)
        assertEquals(2L, match.captainId)
        assertEquals(5, match.startingLineupIds.size)
        assertEquals(1, match.substituteIds.size) // 6 - 5 = 1 substitute
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
        assertEquals(1000L, match.date)
        assertEquals(0L, match.time) // 00:00 should be 0L, not null
        assertEquals(6, match.squadCallUpIds.size)
        assertEquals(2L, match.captainId)
        assertEquals(5, match.startingLineupIds.size)
        assertEquals(1, match.substituteIds.size)
    }
}
