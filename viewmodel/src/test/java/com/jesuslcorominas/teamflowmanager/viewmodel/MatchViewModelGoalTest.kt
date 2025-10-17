package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetGoalsForMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class MatchViewModelGoalTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getMatchUseCase: GetMatchUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var finishMatchUseCase: FinishMatchUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
    private lateinit var startMatchTimerUseCase: StartMatchTimerUseCase
    private lateinit var startPlayerTimerUseCase: StartPlayerTimerUseCase
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase
    private lateinit var getMatchSummaryUseCase: GetMatchSummaryUseCase
    private lateinit var registerGoalUseCase: RegisterGoalUseCase
    private lateinit var getGoalsForMatchUseCase: GetGoalsForMatchUseCase
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var timeTicker: TimeTicker
    private lateinit var viewModel: MatchViewModel

    private val match =
        Match(
            id = 1L,
            teamId = 1L,
            elapsedTimeMillis = 900000L,
            isRunning = true,
            lastStartTimeMillis = System.currentTimeMillis() - 60000L,
        )

    private val players =
        listOf(
            Player(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                number = 10,
                positions = listOf(Position.Forward),
                teamId = 1L,
            ),
            Player(
                id = 2L,
                firstName = "Jane",
                lastName = "Smith",
                number = 5,
                positions = listOf(Position.Defender),
                teamId = 1L,
            ),
        )

    private val playerTimes =
        listOf(
            PlayerTime(
                playerId = 1L,
                elapsedTimeMillis = 450000L,
                isRunning = true,
                lastStartTimeMillis = System.currentTimeMillis() - 30000L,
            ),
            PlayerTime(
                playerId = 2L,
                elapsedTimeMillis = 300000L,
                isRunning = false,
                lastStartTimeMillis = null,
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchUseCase = mockk()
        getAllPlayerTimesUseCase = mockk()
        getPlayersUseCase = mockk()
        finishMatchUseCase = mockk(relaxed = true)
        pauseMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
        startMatchTimerUseCase = mockk(relaxed = true)
        startPlayerTimerUseCase = mockk(relaxed = true)
        registerPlayerSubstitutionUseCase = mockk(relaxed = true)
        getMatchSummaryUseCase = mockk(relaxed = true)
        registerGoalUseCase = mockk(relaxed = true)
        getGoalsForMatchUseCase = mockk()
        preferencesRepository = mockk(relaxed = true)
        timeTicker = TestTimeTicker()

        every { getMatchUseCase() } returns flowOf(match)
        every { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)
        every { getPlayersUseCase() } returns flowOf(players)
        every { getGoalsForMatchUseCase(any()) } returns flowOf(emptyList())

        viewModel =
            MatchViewModel(
                getMatchUseCase,
                getAllPlayerTimesUseCase,
                getPlayersUseCase,
                finishMatchUseCase,
                pauseMatchUseCase,
                resumeMatchUseCase,
                startMatchTimerUseCase,
                startPlayerTimerUseCase,
                registerPlayerSubstitutionUseCase,
                getMatchSummaryUseCase,
                registerGoalUseCase,
                getGoalsForMatchUseCase,
                preferencesRepository,
                timeTicker,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `showGoalScorerDialog should set showGoalScorerDialog to true`() =
        runTest(testDispatcher) {
            // When
            viewModel.showGoalScorerDialog()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.showGoalScorerDialog.value)
        }

    @Test
    fun `dismissGoalScorerDialog should set showGoalScorerDialog to false`() =
        runTest(testDispatcher) {
            // Given
            viewModel.showGoalScorerDialog()
            advanceUntilIdle()

            // When
            viewModel.dismissGoalScorerDialog()
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.showGoalScorerDialog.value)
        }

    @Test
    fun `registerGoal should call registerGoalUseCase and dismiss dialog`() =
        runTest(testDispatcher) {
            // Given
            val scorerId = 1L
            coEvery { registerGoalUseCase(any(), any(), any()) } returns 1L

            viewModel.showGoalScorerDialog()
            advanceUntilIdle()

            // When
            viewModel.registerGoal(scorerId)
            advanceUntilIdle()

            // Then
            coVerify { registerGoalUseCase(match.id, scorerId, any()) }
            assertFalse(viewModel.showGoalScorerDialog.value)
        }

    @Test
    fun `goals count should be reflected in UI state`() =
        runTest(testDispatcher) {
            // Given
            val goals =
                listOf(
                    Goal(
                        id = 1L,
                        matchId = match.id,
                        scorerId = 1L,
                        goalTimeMillis = 1000L,
                        matchElapsedTimeMillis = 500000L,
                        isOpponentGoal = false,
                    ),
                    Goal(
                        id = 2L,
                        matchId = match.id,
                        scorerId = 2L,
                        goalTimeMillis = 2000L,
                        matchElapsedTimeMillis = 900000L,
                        isOpponentGoal = false,
                    ),
                )
            every { getGoalsForMatchUseCase(match.id) } returns flowOf(goals)

            // Recreate viewModel to pick up new goals
            viewModel =
                MatchViewModel(
                    getMatchUseCase,
                    getAllPlayerTimesUseCase,
                    getPlayersUseCase,
                    finishMatchUseCase,
                    pauseMatchUseCase,
                    resumeMatchUseCase,
                    startMatchTimerUseCase,
                    startPlayerTimerUseCase,
                    registerPlayerSubstitutionUseCase,
                    getMatchSummaryUseCase,
                    registerGoalUseCase,
                    getGoalsForMatchUseCase,
                    preferencesRepository,
                    timeTicker,
                )

            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchUiState.Success)
            assertEquals(2, (state as MatchUiState.Success).goalsCount)
        }

    @Test
    fun `showOpponentGoalDialog should set showOpponentGoalDialog to true`() =
        runTest(testDispatcher) {
            // When
            viewModel.showOpponentGoalDialog()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.showOpponentGoalDialog.value)
        }

    @Test
    fun `dismissOpponentGoalDialog should set showOpponentGoalDialog to false`() =
        runTest(testDispatcher) {
            // Given
            viewModel.showOpponentGoalDialog()
            advanceUntilIdle()

            // When
            viewModel.dismissOpponentGoalDialog()
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.showOpponentGoalDialog.value)
        }

    @Test
    fun `registerOpponentGoal should call registerGoalUseCase with isOpponentGoal true`() =
        runTest(testDispatcher) {
            // Given
            coEvery { registerGoalUseCase(any(), any(), any(), any()) } returns 1L

            viewModel.showOpponentGoalDialog()
            advanceUntilIdle()

            // When
            viewModel.registerOpponentGoal()
            advanceUntilIdle()

            // Then
            coVerify { registerGoalUseCase(match.id, null, any(), true) }
            assertFalse(viewModel.showOpponentGoalDialog.value)
        }

    @Test
    fun `opponent goals count should be reflected in UI state`() =
        runTest(testDispatcher) {
            // Given
            val goals =
                listOf(
                    Goal(
                        id = 1L,
                        matchId = match.id,
                        scorerId = 1L,
                        goalTimeMillis = 1000L,
                        matchElapsedTimeMillis = 500000L,
                        isOpponentGoal = false,
                    ),
                    Goal(
                        id = 2L,
                        matchId = match.id,
                        scorerId = null,
                        goalTimeMillis = 2000L,
                        matchElapsedTimeMillis = 700000L,
                        isOpponentGoal = true,
                    ),
                    Goal(
                        id = 3L,
                        matchId = match.id,
                        scorerId = null,
                        goalTimeMillis = 3000L,
                        matchElapsedTimeMillis = 900000L,
                        isOpponentGoal = true,
                    ),
                )
            every { getGoalsForMatchUseCase(match.id) } returns flowOf(goals)

            // Recreate viewModel to pick up new goals
            viewModel =
                MatchViewModel(
                    getMatchUseCase,
                    getAllPlayerTimesUseCase,
                    getPlayersUseCase,
                    finishMatchUseCase,
                    pauseMatchUseCase,
                    resumeMatchUseCase,
                    startMatchTimerUseCase,
                    startPlayerTimerUseCase,
                    registerPlayerSubstitutionUseCase,
                    getMatchSummaryUseCase,
                    registerGoalUseCase,
                    getGoalsForMatchUseCase,
                    preferencesRepository,
                    timeTicker,
                )

            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchUiState.Success)
            assertEquals(1, (state as MatchUiState.Success).goalsCount)
            assertEquals(2, state.opponentGoalsCount)
        }

    private class TestTimeTicker : TimeTicker {
        private val currentTime = MutableStateFlow(System.currentTimeMillis())
        override val timeFlow: Flow<Long> = currentTime
    }
}
