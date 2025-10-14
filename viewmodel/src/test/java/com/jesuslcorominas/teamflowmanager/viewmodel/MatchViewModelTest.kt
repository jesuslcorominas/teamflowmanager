package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class MatchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getMatchUseCase: GetMatchUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var finishMatchUseCase: FinishMatchUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: MatchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchUseCase = mockk()
        getAllPlayerTimesUseCase = mockk()
        getPlayersUseCase = mockk()
        finishMatchUseCase = mockk(relaxed = true)
        pauseMatchUseCase = mockk(relaxed = true)
        resumeMatchUseCase = mockk(relaxed = true)
        registerPlayerSubstitutionUseCase = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest(testDispatcher) {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pauseMatch should call pauseMatchUseCase with current time`() = runTest(testDispatcher) {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // When
        viewModel.pauseMatch()

        // Then
        coVerify { pauseMatchUseCase(any()) }
    }

    @Test
    fun `resumeMatch should call resumeMatchUseCase with current time`() = runTest(testDispatcher) {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // When
        viewModel.resumeMatch()

        // Then
        coVerify { resumeMatchUseCase(any()) }
    }

    @Test
    fun `selectPlayerOut should update selected player out state`() = runTest(testDispatcher) {
        // Given
        val match = Match(
            id = 1L,
            elapsedTimeMillis = 300000L,
            isRunning = true,
            lastStartTimeMillis = System.currentTimeMillis(),
        )
        val player = Player(5L, "John", "Doe", 10, listOf(Position.Forward))
        val playerTime = PlayerTime(5L, 150000L, true, System.currentTimeMillis())
        
        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(listOf(playerTime))
        every { getPlayersUseCase.invoke() } returns flowOf(listOf(player))

        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // When
        viewModel.selectedPlayerOut.test(timeout = 2.seconds) {
            assertEquals(null, awaitItem())
            viewModel.selectPlayerOut(5L)
            assertEquals(5L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearPlayerOutSelection should clear selected player out state`() = runTest(testDispatcher) {
        // Given
        val match = Match(
            id = 1L,
            elapsedTimeMillis = 300000L,
            isRunning = true,
            lastStartTimeMillis = System.currentTimeMillis(),
        )
        val player = Player(5L, "John", "Doe", 10, listOf(Position.Forward))
        val playerTime = PlayerTime(5L, 150000L, true, System.currentTimeMillis())
        
        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(listOf(playerTime))
        every { getPlayersUseCase.invoke() } returns flowOf(listOf(player))

        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // When/Then
        viewModel.selectedPlayerOut.test(timeout = 2.seconds) {
            assertEquals(null, awaitItem())
            viewModel.selectPlayerOut(5L)
            assertEquals(5L, awaitItem())
            viewModel.clearPlayerOutSelection()
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `substitutePlayer should call registerPlayerSubstitutionUseCase and clear selection`() =
        runTest(testDispatcher) {
            // Given
            val match = Match(
                id = 1L,
                elapsedTimeMillis = 300000L,
                isRunning = true,
                lastStartTimeMillis = System.currentTimeMillis(),
            )
            val player2 = Player(2L, "Jane", "Smith", 7, listOf(Position.Midfielder))
            val player3 = Player(3L, "Bob", "Johnson", 5, listOf(Position.Defender))
            val playerTime2 = PlayerTime(2L, 150000L, true, System.currentTimeMillis())
            
            every { getMatchUseCase.invoke() } returns flowOf(match)
            every { getAllPlayerTimesUseCase.invoke() } returns flowOf(listOf(playerTime2))
            every { getPlayersUseCase.invoke() } returns flowOf(listOf(player2, player3))

            viewModel = MatchViewModel(
                getMatchUseCase = getMatchUseCase,
                getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
                getPlayersUseCase = getPlayersUseCase,
                saveMatchUseCase = finishMatchUseCase,
                pauseMatchUseCase = pauseMatchUseCase,
                resumeMatchUseCase = resumeMatchUseCase,
                registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
                preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
            )

            // When/Then
            viewModel.selectedPlayerOut.test(timeout = 2.seconds) {
                assertEquals(null, awaitItem())
                viewModel.selectPlayerOut(2L)
                assertEquals(2L, awaitItem())
                viewModel.substitutePlayer(3L)
                assertEquals(null, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerify { registerPlayerSubstitutionUseCase(1L, 2L, 3L, any()) }
        }

    @Test
    fun `uiState should be NoMatch when match is null`() = runTest(testDispatcher) {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            assertEquals(MatchUiState.NoMatch, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should be Success when match exists`() = runTest(testDispatcher) {
        // Given
        val match = Match(
            id = 1L,
            elapsedTimeMillis = 300000L,
            isRunning = false,
            lastStartTimeMillis = null,
        )
        val players = listOf(
            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1),
            Player(2, "Jane", "Smith", 8, listOf(Position.Midfielder), 1),
        )
        val playerTimes = listOf(
            PlayerTime(1, 150000L, false, null),
            PlayerTime(2, 100000L, false, null),
        )

        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is MatchUiState.Success)
            val successState = state as MatchUiState.Success
            assertEquals(300000L, successState.matchTimeMillis)
            assertEquals(false, successState.matchIsRunning)
            assertEquals(2, successState.playerTimes.size)
            assertEquals(150000L, successState.playerTimes[0].timeMillis)
            assertEquals(100000L, successState.playerTimes[1].timeMillis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `success state should include players without timer`() = runTest(testDispatcher) {
        // Given
        val match = Match(1L, 0L, isRunning = false, lastStartTimeMillis = null)
        val players = listOf(
            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1),
            Player(2, "Jane", "Smith", 8, listOf(Position.Midfielder), 1),
        )
        val playerTimes = listOf(
            PlayerTime(1, 50000L, false, null),
        )

        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            val state = awaitItem() as MatchUiState.Success
            assertEquals(2, state.playerTimes.size)
            assertEquals(50000L, state.playerTimes[0].timeMillis)
            assertEquals(0L, state.playerTimes[1].timeMillis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `running match time should be calculated correctly`() = runTest(testDispatcher) {
        // Given
        val currentTime = System.currentTimeMillis()
        val match = Match(
            id = 1L,
            elapsedTimeMillis = 300000L,
            isRunning = true,
            lastStartTimeMillis = currentTime - 60000L,
        )
        val players = emptyList<Player>()
        val playerTimes = emptyList<PlayerTime>()

        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            val state = awaitItem() as MatchUiState.Success
            assertTrue(state.matchIsRunning)
            assertTrue(state.matchTimeMillis >= 360000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `running player time should be calculated correctly`() = runTest(testDispatcher) {
        // Given
        val currentTime = System.currentTimeMillis()
        val match = Match(1L, 0L, isRunning = false, lastStartTimeMillis = null)
        val players = listOf(
            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1),
        )
        val playerTimes = listOf(
            PlayerTime(
                playerId = 1,
                elapsedTimeMillis = 100000L,
                isRunning = true,
                lastStartTimeMillis = currentTime - 30000L,
            ),
        )

        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            val state = awaitItem() as MatchUiState.Success
            assertTrue(state.playerTimes[0].isRunning)
            assertTrue(state.playerTimes[0].timeMillis >= 130000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `time should update every second`() = runTest(testDispatcher) {
        // Given
        val currentTime = System.currentTimeMillis()
        val match = Match(
            id = 1L,
            elapsedTimeMillis = 100000L,
            isRunning = true,
            lastStartTimeMillis = currentTime,
        )
        val players = emptyList<Player>()
        val playerTimes = emptyList<PlayerTime>()

        every { getMatchUseCase.invoke() } returns flowOf(match)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
        every { getPlayersUseCase.invoke() } returns flowOf(players)

        // When
        viewModel = MatchViewModel(
            getMatchUseCase = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatchUseCase = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            preferencesRepository = preferencesRepository,
            dispatcher = testDispatcher
        )

        // Then
        viewModel.uiState.test(timeout = 3.seconds) {
            assertEquals(MatchUiState.Loading, awaitItem())
            val initialState = awaitItem() as MatchUiState.Success
            val initialTime = initialState.matchTimeMillis
            
            // Wait for at least one update
            val updatedState = awaitItem() as MatchUiState.Success
            assertTrue(updatedState.matchTimeMillis > initialTime)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
