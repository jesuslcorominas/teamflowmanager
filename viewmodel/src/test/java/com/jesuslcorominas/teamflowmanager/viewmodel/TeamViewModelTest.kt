package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import androidx.lifecycle.SavedStateHandle
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.HasScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
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
class TeamViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var createTeamUseCase: CreateTeamUseCase
    private lateinit var updateTeamUseCase: UpdateTeamUseCase
    private lateinit var getCaptainPlayerUseCase: GetCaptainPlayerUseCase
    private lateinit var hasScheduledMatchesUseCase: HasScheduledMatchesUseCase
    private lateinit var setPlayerAsCaptainUseCase: SetPlayerAsCaptainUseCase
    private lateinit var removePlayerAsCaptainUseCase: RemovePlayerAsCaptainUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: TeamViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        getPlayersUseCase = mockk()
        createTeamUseCase = mockk(relaxed = true)
        updateTeamUseCase = mockk(relaxed = true)
        getCaptainPlayerUseCase = mockk()
        hasScheduledMatchesUseCase = mockk()
        setPlayerAsCaptainUseCase = mockk(relaxed = true)
        removePlayerAsCaptainUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getTeamUseCase.invoke() } returns flowOf(null)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())

        // When
        viewModel = TeamViewModel(
            getTeam = getTeamUseCase,
            getPlayers = getPlayersUseCase,
            createTeam = createTeamUseCase,
            updateTeam = updateTeamUseCase,
            getCaptainPlayer = getCaptainPlayerUseCase,
            hasScheduledMatches = hasScheduledMatchesUseCase,
            setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
            removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
            analyticsTracker = analyticsTracker,
            savedStateHandle = savedStateHandle
        )

        // Then
        assertEquals(TeamUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be NoTeam when no team exists`() =
        runTest(testDispatcher) {
            // Given
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())

            // When
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            advanceUntilIdle()

            // Then
            assertEquals(TeamUiState.NoTeam, viewModel.uiState.value)
        }

    @Test
    fun `uiState should be Success when team exists`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(team)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())

            // When
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(TeamUiState.Success(team, emptyList()), state)
        }

    @Test
    fun `createTeam should call createTeamUseCase with correct parameters`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(0, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            coEvery { createTeamUseCase.invoke(any()) } just runs
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            val onSuccess: () -> Unit = mockk(relaxed = true)

            // When
            viewModel.createTeam(team, onSuccess)
            advanceUntilIdle()

            // Then
            coVerify { createTeamUseCase.invoke(team) }
        }

    @Test
    fun `updateTeam should call updateTeamUseCase with correct team`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(1, "Updated Team", "Updated Coach", "Updated Delegate", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(team)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            coEvery { updateTeamUseCase.invoke(any()) } just runs
            coEvery { getCaptainPlayerUseCase.invoke() } returns null
            coEvery { hasScheduledMatchesUseCase.invoke() } returns false
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            val onSuccess: () -> Unit = mockk(relaxed = true)

            // When
            viewModel.updateTeam(team, null, onSuccess)
            advanceUntilIdle()

            // Then
            coVerify { updateTeamUseCase.invoke(team) }
        }
}
