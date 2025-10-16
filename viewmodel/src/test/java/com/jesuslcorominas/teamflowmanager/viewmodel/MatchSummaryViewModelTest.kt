package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.MatchSummary
import com.jesuslcorominas.teamflowmanager.usecase.PlayerTimeSummary
import com.jesuslcorominas.teamflowmanager.usecase.SubstitutionSummary
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchSummaryViewModelTest {
    private lateinit var getMatchSummaryUseCase: GetMatchSummaryUseCase
    private lateinit var viewModel: MatchSummaryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchSummaryUseCase = mockk()
        viewModel = MatchSummaryViewModel(
            getMatchSummaryUseCase = getMatchSummaryUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMatchSummary should update uiState to NotFound when summary is null`() =
        runTest {
            // Given
            val matchId = 1L
            every { getMatchSummaryUseCase(matchId) } returns flowOf(null)

            // When
            viewModel.loadMatchSummary(matchId)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is MatchSummaryUiState.NotFound)
        }

    @Test
    fun `loadMatchSummary should update uiState to Success when summary is available`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                elapsedTimeMillis = 3000000L,
            )
            val player1 = Player(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                number = 10,
                positions = listOf(Position.Forward),
                teamId = 1L,
            )
            val player2 = Player(
                id = 2L,
                firstName = "Jane",
                lastName = "Smith",
                number = 5,
                positions = listOf(Position.Defender),
                teamId = 1L,
            )
            val summary = MatchSummary(
                match = match,
                playerTimes = listOf(
                    PlayerTimeSummary(player = player1, elapsedTimeMillis = 1500000L, substitutionCount = 2),
                    PlayerTimeSummary(player = player2, elapsedTimeMillis = 2000000L, substitutionCount = 1),
                ),
                substitutions = listOf(
                    SubstitutionSummary(
                        playerOut = player1,
                        playerIn = player2,
                        matchElapsedTimeMillis = 900000L,
                    ),
                ),
            )
            every { getMatchSummaryUseCase(matchId) } returns flowOf(summary)

            // When
            viewModel.loadMatchSummary(matchId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is MatchSummaryUiState.Success)
            val successState = state as MatchSummaryUiState.Success
            assertEquals(matchId, successState.matchId)
            assertEquals("Team A", successState.opponent)
            assertEquals("Stadium", successState.location)
            assertEquals(3000000L, successState.matchTimeMillis)
            assertEquals(2, successState.playerTimes.size)
            assertEquals(1, successState.substitutions.size)
        }

    @Test
    fun `initial uiState should be Loading`() {
        // Then
        assertTrue(viewModel.uiState.value is MatchSummaryUiState.Loading)
    }
}
