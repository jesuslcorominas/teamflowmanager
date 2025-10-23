package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

//@ExperimentalCoroutinesApi
//class MatchViewModelTest {
//
//    private val testDispatcher = StandardTestDispatcher()
//    private lateinit var getMatchUseCase: GetMatchUseCase
//    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
//    private lateinit var getPlayersUseCase: GetPlayersUseCase
//    private lateinit var finishMatchUseCase: FinishMatchUseCase
//    private lateinit var pauseMatchUseCase: PauseMatchUseCase
//    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
//    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase
//
//    private lateinit var preferencesRepository: PreferencesRepository
//    private lateinit var viewModel: MatchViewModel
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        getMatchUseCase = mockk()
//        getAllPlayerTimesUseCase = mockk()
//        getPlayersUseCase = mockk()
//        finishMatchUseCase = mockk(relaxed = true)
//        pauseMatchUseCase = mockk(relaxed = true)
//        resumeMatchUseCase = mockk(relaxed = true)
//        registerPlayerSubstitutionUseCase = mockk(relaxed = true)
//        preferencesRepository = mockk(relaxed = true)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `initial state should be Loading`() = runTest(testDispatcher) {
//        // Given
//        every { getMatchUseCase.invoke() } returns flowOf(null)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
//        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 2.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `pauseMatch should call pauseMatchUseCase with current time`() = runTest(testDispatcher) {
//        // Given
//        every { getMatchUseCase.invoke() } returns flowOf(null)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
//        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
//
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // When
//        viewModel.pauseMatch()
//
//        // Then
//        coVerify { pauseMatchUseCase(any()) }
//    }
//
//    @Test
//    fun `resumeMatch should call resumeMatchUseCase with current time`() = runTest(testDispatcher) {
//        // Given
//        every { getMatchUseCase.invoke() } returns flowOf(null)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
//        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
//
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // When
//        viewModel.resumeMatch()
//
//        // Then
//        coVerify { resumeMatchUseCase(any()) }
//    }
//
//    @Test
//    fun `selectPlayerOut should update selected player out state`() = runTest(testDispatcher) {
//        // Given
//        val match = Match(
//            id = 1L,
//            elapsedTimeMillis = 300000L,
//            isRunning = true,
//            lastStartTimeMillis = System.currentTimeMillis(),
//        )
//        val player = Player(5L, "John", "Doe", 10, listOf(Position.Forward))
//        val playerTime = PlayerTime(5L, 150000L, true, System.currentTimeMillis())
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(listOf(playerTime))
//        every { getPlayersUseCase.invoke() } returns flowOf(listOf(player))
//
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // When
//        viewModel.selectedPlayerOut.test(timeout = 2.seconds) {
//            assertEquals(null, awaitItem())
//            viewModel.selectPlayerOut(5L)
//            assertEquals(5L, awaitItem())
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `clearPlayerOutSelection should clear selected player out state`() = runTest(testDispatcher) {
//        // Given
//        val match = Match(
//            id = 1L,
//            elapsedTimeMillis = 300000L,
//            isRunning = true,
//            lastStartTimeMillis = System.currentTimeMillis(),
//        )
//        val player = Player(5L, "John", "Doe", 10, listOf(Position.Forward))
//        val playerTime = PlayerTime(5L, 150000L, true, System.currentTimeMillis())
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(listOf(playerTime))
//        every { getPlayersUseCase.invoke() } returns flowOf(listOf(player))
//
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // When/Then
//        viewModel.selectedPlayerOut.test(timeout = 2.seconds) {
//            assertEquals(null, awaitItem())
//            viewModel.selectPlayerOut(5L)
//            assertEquals(5L, awaitItem())
//            viewModel.clearPlayerOutSelection()
//            assertEquals(null, awaitItem())
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `substitutePlayer should call registerPlayerSubstitutionUseCase and clear selection`() =
//        runTest(testDispatcher) {
//            // Given
//            val match = Match(
//                id = 1L,
//                elapsedTimeMillis = 300000L,
//                isRunning = true,
//                lastStartTimeMillis = System.currentTimeMillis(),
//            )
//            val player2 = Player(2L, "Jane", "Smith", 7, listOf(Position.Midfielder))
//            val player3 = Player(3L, "Bob", "Johnson", 5, listOf(Position.Defender))
//            val playerTime2 = PlayerTime(2L, 150000L, true, System.currentTimeMillis())
//
//            every { getMatchUseCase.invoke() } returns flowOf(match)
//            every { getAllPlayerTimesUseCase.invoke() } returns flowOf(listOf(playerTime2))
//            every { getPlayersUseCase.invoke() } returns flowOf(listOf(player2, player3))
//
//            viewModel = MatchViewModel(
//                getMatchUseCase = getMatchUseCase,
//                getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//                getPlayersUseCase = getPlayersUseCase,
//                saveMatchUseCase = finishMatchUseCase,
//                pauseMatchUseCase = pauseMatchUseCase,
//                resumeMatchUseCase = resumeMatchUseCase,
//                registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//                preferencesRepository = preferencesRepository
//            )
//
//            // When/Then
//            viewModel.selectedPlayerOut.test(timeout = 2.seconds) {
//                assertEquals(null, awaitItem())
//                viewModel.selectPlayerOut(2L)
//                assertEquals(2L, awaitItem())
//                viewModel.substitutePlayer(3L)
//                assertEquals(null, awaitItem())
//                cancelAndIgnoreRemainingEvents()
//            }
//
//            // Then
//            coVerify { registerPlayerSubstitutionUseCase(1L, 2L, 3L, any()) }
//        }
//
//    @Test
//    fun `uiState should be NoMatch when match is null`() = runTest(testDispatcher) {
//        // Given
//        every { getMatchUseCase.invoke() } returns flowOf(null)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
//        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 2.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            assertEquals(MatchUiState.NoMatch, awaitItem())
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `uiState should be Success when match exists`() = runTest(testDispatcher) {
//        // Given
//        val match = Match(
//            id = 1L,
//            elapsedTimeMillis = 300000L,
//            isRunning = false,
//            lastStartTimeMillis = null,
//        )
//        val players = listOf(
//            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1),
//            Player(2, "Jane", "Smith", 8, listOf(Position.Midfielder), 1),
//        )
//        val playerTimes = listOf(
//            PlayerTime(1, 150000L, false, null),
//            PlayerTime(2, 100000L, false, null),
//        )
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
//        every { getPlayersUseCase.invoke() } returns flowOf(players)
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 2.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            val state = awaitItem()
//            assertTrue(state is MatchUiState.Success)
//            val successState = state as MatchUiState.Success
//            assertEquals(300000L, successState.matchTimeMillis)
//            assertEquals(false, successState.matchIsRunning)
//            assertEquals(2, successState.playerTimes.size)
//            assertEquals(150000L, successState.playerTimes[0].timeMillis)
//            assertEquals(100000L, successState.playerTimes[1].timeMillis)
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `success state should include players without timer`() = runTest(testDispatcher) {
//        // Given
//        val match = Match(1L, 0L, isRunning = false, lastStartTimeMillis = null)
//        val players = listOf(
//            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1),
//            Player(2, "Jane", "Smith", 8, listOf(Position.Midfielder), 1),
//        )
//        val playerTimes = listOf(
//            PlayerTime(1, 50000L, false, null),
//        )
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
//        every { getPlayersUseCase.invoke() } returns flowOf(players)
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 2.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            val state = awaitItem() as MatchUiState.Success
//            assertEquals(2, state.playerTimes.size)
//            assertEquals(50000L, state.playerTimes[0].timeMillis)
//            assertEquals(0L, state.playerTimes[1].timeMillis)
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `running match time should be calculated correctly`() = runTest(testDispatcher) {
//        // Given
//        val currentTime = System.currentTimeMillis()
//        val match = Match(
//            id = 1L,
//            elapsedTimeMillis = 300000L,
//            isRunning = true,
//            lastStartTimeMillis = currentTime - 60000L,
//        )
//        val players = emptyList<Player>()
//        val playerTimes = emptyList<PlayerTime>()
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
//        every { getPlayersUseCase.invoke() } returns flowOf(players)
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 2.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            val state = awaitItem() as MatchUiState.Success
//            assertTrue(state.matchIsRunning)
//            assertTrue(state.matchTimeMillis >= 360000L)
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `running player time should be calculated correctly`() = runTest(testDispatcher) {
//        // Given
//        val currentTime = System.currentTimeMillis()
//        val match = Match(1L, 0L, isRunning = false, lastStartTimeMillis = null)
//        val players = listOf(
//            Player(1, "John", "Doe", 10, listOf(Position.Forward), 1),
//        )
//        val playerTimes = listOf(
//            PlayerTime(
//                playerId = 1,
//                elapsedTimeMillis = 100000L,
//                isRunning = true,
//                lastStartTimeMillis = currentTime - 30000L,
//            ),
//        )
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
//        every { getPlayersUseCase.invoke() } returns flowOf(players)
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 2.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            val state = awaitItem() as MatchUiState.Success
//            assertTrue(state.playerTimes[0].isRunning)
//            assertTrue(state.playerTimes[0].timeMillis >= 130000L)
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `time should update every second`() = runTest(testDispatcher) {
//        // Given
//        val currentTime = System.currentTimeMillis()
//        val match = Match(
//            id = 1L,
//            elapsedTimeMillis = 100000L,
//            isRunning = true,
//            lastStartTimeMillis = currentTime,
//        )
//        val players = emptyList<Player>()
//        val playerTimes = emptyList<PlayerTime>()
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
//        every { getPlayersUseCase.invoke() } returns flowOf(players)
//
//        // When
//        viewModel = MatchViewModel(
//            getMatchUseCase = getMatchUseCase,
//            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
//            getPlayersUseCase = getPlayersUseCase,
//            saveMatchUseCase = finishMatchUseCase,
//            pauseMatchUseCase = pauseMatchUseCase,
//            resumeMatchUseCase = resumeMatchUseCase,
//            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
//            preferencesRepository = preferencesRepository
//        )
//
//        // Then
//        viewModel.uiState.test(timeout = 3.seconds) {
//            assertEquals(MatchUiState.Loading, awaitItem())
//            val initialState = awaitItem() as MatchUiState.Success
//            val initialTime = initialState.matchTimeMillis
//
//            // Wait for at least one update
//            val updatedState = awaitItem() as MatchUiState.Success
//            assertTrue(updatedState.matchTimeMillis > initialTime)
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MatchViewModelTest {

    private val getMatchUseCase: GetMatchUseCase = mockk()
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase = mockk()
    private val getPlayersUseCase: GetPlayersUseCase = mockk()
    private val finishMatchUseCase: FinishMatchUseCase = mockk()
    private val pauseMatchUseCase: PauseMatchUseCase = mockk()
    private val resumeMatchUseCase: ResumeMatchUseCase = mockk()
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase = mockk()
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()

    private lateinit var fakeTicker: FakeTimeTicker
    private lateinit var viewModel: MatchViewModel

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val fakeMatch = Match(
        id = 1L,
        teamName = "My Team",
        elapsedTimeMillis = 10_000L,
        isRunning = true,
        lastStartTimeMillis = 1_000L,
    )

    private val fakePlayers = listOf(
        Player(1L, "John", "Doe", 10, listOf(Position.Forward)),
        Player(2L, "John", "Doe", 11, listOf(Position.Forward))
    )

    private val fakePlayerTimes = listOf(
        PlayerTime(playerId = 1L, elapsedTimeMillis = 5_000L, isRunning = true, lastStartTimeMillis = 1_000L),
        PlayerTime(playerId = 2L, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null)
    )

    @Before
    fun setup() {
        every { getMatchUseCase() } returns flowOf(fakeMatch)
        every { getAllPlayerTimesUseCase() } returns flowOf(fakePlayerTimes)
        every { getPlayersUseCase() } returns flowOf(fakePlayers)
        every { preferencesRepository.shouldShowInvalidSubstitutionAlert() } returns true
        coEvery { preferencesRepository.setShouldShowInvalidSubstitutionAlert(any()) } just runs
        coEvery { finishMatchUseCase() } just runs
        coEvery { pauseMatchUseCase(any()) } just runs
        coEvery { resumeMatchUseCase(any(), any()) } just runs
        coEvery { registerPlayerSubstitutionUseCase(any(), any(), any(), any()) } just runs

        fakeTicker = FakeTimeTicker()

        viewModel = MatchViewModel(
            getMatchById = getMatchUseCase,
            getAllPlayerTimesUseCase = getAllPlayerTimesUseCase,
            getPlayersUseCase = getPlayersUseCase,
            saveMatchUseCase = finishMatchUseCase,
            pauseMatch = pauseMatchUseCase,
            resumeMatchUseCase = resumeMatchUseCase,
            registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
            getMatchSummaryUseCase = getMatchSummaryUseCase,
            preferencesRepository = preferencesRepository,
            timeTicker = fakeTicker,
            startMatchTimerUseCase = TODO(),
            startPlayerTimerUseCase = TODO(),
            registerGoal = TODO(),
            getGoalsForMatchUseCase = TODO(),
        )
    }

    // ----------- TESTS -----------

    @Test
    fun `loadMatchData - emits Success when data available`() = runTest {
        fakeTicker.emit(System.currentTimeMillis())

        viewModel.uiState.test {
            skipItems(1) // salta el estado Loading inicial
            val state = awaitItem()
            assertTrue(state is MatchUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveMatch - calls FinishMatchUseCase`() = runTest {
        val successState = MatchUiState.Success(
            matchId = 1L,
            teamName = "My Team",
            opponent = "Other team",
            matchTimeMillis = 0L,
            matchIsRunning = true,
            playerTimes = emptyList(),
        )
        val uiStateField = MatchViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        (uiStateField.get(viewModel) as MutableStateFlow<MatchUiState>).value = successState

        viewModel.saveMatch()
        advanceUntilIdle()

        coVerify { finishMatchUseCase() }
    }

    @Test
    fun `pauseMatch - calls PauseMatchUseCase with current time`() = runTest {
        // Forzamos un estado Success válido
        val successState = MatchUiState.Success(
            matchId = 1L,
            teamName = "My Team",
            opponent = "Other team",
            matchTimeMillis = 0L,
            matchIsRunning = true,
            playerTimes = emptyList()
        )
        val uiStateField = MatchViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        (uiStateField.get(viewModel) as MutableStateFlow<MatchUiState>).value = successState

        val before = System.currentTimeMillis()
        viewModel.pauseMatch()
        advanceUntilIdle()

        coVerify { pauseMatchUseCase(withArg { assertTrue(it >= before) }) }
    }


    @Test
    fun `resumeMatch - calls ResumeMatchUseCase with current time`() = runTest {
        val successState = MatchUiState.Success(
            matchId = 1L,
            teamName = "My Team",
            opponent = "Other team",
            matchTimeMillis = 0L,
            matchIsRunning = true,
            playerTimes = emptyList()
        )
        val uiStateField = MatchViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        (uiStateField.get(viewModel) as MutableStateFlow<MatchUiState>).value = successState

        val before = System.currentTimeMillis()
        viewModel.resumeMatch()
        advanceUntilIdle()

        coVerify { resumeMatchUseCase(any(), withArg { it -> assertTrue(it >= before) }) }
    }

    @Test
    fun `selectPlayerOut - selects running player`() = runTest {
        val successState = MatchUiState.Success(
            matchId = 1L,
            teamName = "My Team",
            opponent = "Other team",
            matchTimeMillis = 0L,
            matchIsRunning = true,
            playerTimes = listOf(
                PlayerTimeItem(player = fakePlayers[0], timeMillis = 1000L, isRunning = true)
            )
        )
        val uiStateField = MatchViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        (uiStateField.get(viewModel) as MutableStateFlow<MatchUiState>).value = successState

        viewModel.selectPlayerOut(1L)

        viewModel.selectedPlayerOut.test {
            assertEquals(1L, awaitItem())
        }
    }

    @Test
    fun `selectPlayerOut - shows invalid substitution alert when player not running`() = runTest {
        val successState = MatchUiState.Success(
            matchId = 1L,
            teamName = "My Team",
            opponent = "Other team",
            matchTimeMillis = 0L,
            matchIsRunning = true,
            playerTimes = listOf(
                PlayerTimeItem(player = fakePlayers[0], timeMillis = 1000L, isRunning = false)
            )
        )
        val uiStateField = MatchViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        (uiStateField.get(viewModel) as MutableStateFlow<MatchUiState>).value = successState

        viewModel.selectPlayerOut(1L)

        viewModel.showInvalidSubstitutionAlert.test {
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `dismissInvalidSubstitutionAlert - hides alert and disables future alerts`() = runTest {
        viewModel.dismissInvalidSubstitutionAlert(dontShowAgain = true)

        coVerify { preferencesRepository.setShouldShowInvalidSubstitutionAlert(false) }

        viewModel.showInvalidSubstitutionAlert.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `substitutePlayer - calls RegisterPlayerSubstitutionUseCase and clears selection`() = runTest {
        val successState = MatchUiState.Success(
            matchId = 1L,
            teamName = "My Team",
            opponent = "Other team",
            matchTimeMillis = 0L,
            matchIsRunning = true,
            playerTimes = listOf(
                PlayerTimeItem(player = fakePlayers[0], timeMillis = 1000L, isRunning = true)
            )
        )

        val uiStateField = MatchViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        val selectedField =
            MatchViewModel::class.java.getDeclaredField("_selectedPlayerOut").apply { isAccessible = true }

        (uiStateField.get(viewModel) as MutableStateFlow<MatchUiState>).value = successState
        (selectedField.get(viewModel) as MutableStateFlow<Long?>).value = 1L

        viewModel.substitutePlayer(2L)
        advanceUntilIdle()

        coVerify {
            registerPlayerSubstitutionUseCase(
                matchId = 1L,
                playerOutId = 1L,
                playerInId = 2L,
                any()
            )
        }

        assertNull((selectedField.get(viewModel) as MutableStateFlow<Long?>).value)
    }


    @Test
    fun `currentTime updates when ticker emits`() = runTest {
        val now = 1000L
        val later = 2000L

        fakeTicker.emit(now)
        fakeTicker.emit(later)

        val currentTimeField = MatchViewModel::class.java.getDeclaredField("_currentTime").apply { isAccessible = true }
        val flow = currentTimeField.get(viewModel) as MutableStateFlow<Long>

        assertTrue(flow.value >= now)
    }
}


class FakeTimeTicker : TimeTicker {
    private val _flow = MutableSharedFlow<Long>(replay = 1)
    override val timeFlow: Flow<Long> = _flow

    suspend fun emit(time: Long) = _flow.emit(time)
}
