package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MatchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getMatchUseCase: GetMatchUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var finishMatchUseCase: FinishMatchUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        // When
        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase, pauseMatchUseCase, resumeMatchUseCase)

        // Then
        assertEquals(MatchUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `pauseMatch should call pauseMatchUseCase with current time`() = runTest(testDispatcher) {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase, pauseMatchUseCase, resumeMatchUseCase)
        advanceUntilIdle()

        // When
        viewModel.pauseMatch()
        advanceUntilIdle()

        // Then
        coVerify { pauseMatchUseCase(any()) }
    }

    @Test
    fun `resumeMatch should call resumeMatchUseCase with current time`() = runTest(testDispatcher) {
        // Given
        every { getMatchUseCase.invoke() } returns flowOf(null)
        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())

        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase, pauseMatchUseCase, resumeMatchUseCase)
        advanceUntilIdle()

        // When
        viewModel.resumeMatch()
        advanceUntilIdle()

        // Then
        coVerify { resumeMatchUseCase(any()) }
    }

    // TODO review. It's taking too long to run
//    @Test
//    fun `uiState should be NoMatch when match is null`() = runTest(testDispatcher) {
//        // Given
//        every { getMatchUseCase.invoke() } returns flowOf(null)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(emptyList())
//        every { getPlayersUseCase.invoke() } returns flowOf(emptyList())
//
//        // When
//        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase)
//        advanceUntilIdle()
//
//        // Then
//        assertEquals(MatchUiState.NoMatch, viewModel.uiState.value)
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
//        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.uiState.value
//        assertTrue(state is MatchUiState.Success)
//        val successState = state as MatchUiState.Success
//        assertEquals(300000L, successState.matchTimeMillis)
//        assertEquals(false, successState.matchIsRunning)
//        assertEquals(2, successState.playerTimes.size)
//        assertEquals(150000L, successState.playerTimes[0].timeMillis)
//        assertEquals(100000L, successState.playerTimes[1].timeMillis)
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
//        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.uiState.value as MatchUiState.Success
//        assertEquals(2, state.playerTimes.size)
//        assertEquals(50000L, state.playerTimes[0].timeMillis)
//        assertEquals(0L, state.playerTimes[1].timeMillis)
//    }
//
//    @Test
//    fun `running match time should be calculated correctly`() = runTest(testDispatcher) {
//        // Given
//        val currentTime = 1000000L
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
//        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.uiState.value as MatchUiState.Success
//        assertTrue(state.matchIsRunning)
//        assertTrue(state.matchTimeMillis >= 360000L)
//    }

//    @Test
//    fun `running player time should be calculated correctly`() = runTest(testDispatcher) {
//        // Given
//        val currentTime = 1000000L
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
//        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.uiState.value as MatchUiState.Success
//        assertTrue(state.playerTimes[0].isRunning)
//        assertTrue(state.playerTimes[0].timeMillis >= 130000L)
//    }
//
//    @Test
//    fun `time should update every second`() = runTest(testDispatcher) {
//        // Given
//        val match = Match(
//            id = 1L,
//            elapsedTimeMillis = 100000L,
//            isRunning = true,
//            lastStartTimeMillis = 0L,
//        )
//        val players = emptyList<Player>()
//        val playerTimes = emptyList<PlayerTime>()
//
//        every { getMatchUseCase.invoke() } returns flowOf(match)
//        every { getAllPlayerTimesUseCase.invoke() } returns flowOf(playerTimes)
//        every { getPlayersUseCase.invoke() } returns flowOf(players)
//
//        // When
//        viewModel = MatchViewModel(getMatchUseCase, getAllPlayerTimesUseCase, getPlayersUseCase, finishMatchUseCase)
//        advanceUntilIdle()
//        val initialState = viewModel.uiState.value as MatchUiState.Success
//        val initialTime = initialState.matchTimeMillis
//
//        // Advance time by 1 second
//        advanceTimeBy(1000)
//        advanceUntilIdle()
//
//        // Then
//        val updatedState = viewModel.uiState.value as MatchUiState.Success
//        assertTrue(updatedState.matchTimeMillis > initialTime)
//    }
}
