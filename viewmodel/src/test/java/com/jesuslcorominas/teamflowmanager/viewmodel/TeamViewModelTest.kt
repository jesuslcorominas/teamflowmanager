package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class TeamViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var createTeamUseCase: CreateTeamUseCase
    private lateinit var updateTeamUseCase: UpdateTeamUseCase
    private lateinit var viewModel: TeamViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        createTeamUseCase = mockk(relaxed = true)
        updateTeamUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // Given
        every { getTeamUseCase.invoke() } returns flowOf(null)

        // When
        viewModel = TeamViewModel(getTeamUseCase, createTeamUseCase, updateTeamUseCase)

        // Then
        viewModel.uiState.test(timeout = 2.seconds) {
            assertEquals(TeamUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should be NoTeam when no team exists`() =
        runTest(testDispatcher) {
            // Given
            every { getTeamUseCase.invoke() } returns flowOf(null)

            // When
            viewModel = TeamViewModel(getTeamUseCase, createTeamUseCase, updateTeamUseCase)

            // Then
            viewModel.uiState.test(timeout = 2.seconds) {
                assertEquals(TeamUiState.Loading, awaitItem())
                assertEquals(TeamUiState.NoTeam, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState should be TeamExists when team exists`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name")
            every { getTeamUseCase.invoke() } returns flowOf(team)

            // When
            viewModel = TeamViewModel(getTeamUseCase, createTeamUseCase, updateTeamUseCase)

            // Then
            viewModel.uiState.test(timeout = 2.seconds) {
                assertEquals(TeamUiState.Loading, awaitItem())
                val state = awaitItem()
                assertEquals(TeamUiState.TeamExists(team), state)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `createTeam should call createTeamUseCase with correct parameters`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(0, "Test Team", "Coach Name", "Delegate Name")
            every { getTeamUseCase.invoke() } returns flowOf(null)
            coEvery { createTeamUseCase.invoke(any()) } just runs
            viewModel = TeamViewModel(getTeamUseCase, createTeamUseCase, updateTeamUseCase)

            // When
            viewModel.createTeam(team)

            // Then
            coVerify { createTeamUseCase.invoke(team) }
        }

    @Test
    fun `updateTeam should call updateTeamUseCase with correct team`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(1, "Updated Team", "Updated Coach", "Updated Delegate")
            every { getTeamUseCase.invoke() } returns flowOf(team)
            coEvery { updateTeamUseCase.invoke(any()) } just runs
            viewModel = TeamViewModel(getTeamUseCase, createTeamUseCase, updateTeamUseCase)

            // When
            viewModel.updateTeam(team)

            // Then
            coVerify { updateTeamUseCase.invoke(team) }
        }
}
