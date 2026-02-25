package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.AcceptTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AcceptTeamInvitationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var acceptTeamInvitationUseCase: AcceptTeamInvitationUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        acceptTeamInvitationUseCase = mockk()
        getCurrentUserUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(teamId: String? = "team123") = AcceptTeamInvitationViewModel(
        savedStateHandle = if (teamId != null) {
            SavedStateHandle(mapOf("teamId" to teamId))
        } else {
            SavedStateHandle()
        },
        acceptTeamInvitation = acceptTeamInvitationUseCase,
        getCurrentUser = getCurrentUserUseCase,
    )

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getCurrentUserUseCase.invoke() } returns flowOf(null)

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals(AcceptTeamInvitationState.Loading, viewModel.state.value)
    }

    @Test
    fun `processInvitation with null teamId should set Error state`() = runTest(testDispatcher) {
        // When
        val viewModel = createViewModel(teamId = null)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assert(state is AcceptTeamInvitationState.Error)
        assertEquals("Invalid invitation link", (state as AcceptTeamInvitationState.Error).message)
    }

    @Test
    fun `processInvitation when user not authenticated should set NotAuthenticated state`() =
        runTest(testDispatcher) {
            // Given
            every { getCurrentUserUseCase.invoke() } returns flowOf(null)

            // When
            val viewModel = createViewModel("team123")
            advanceUntilIdle()

            // Then
            val state = viewModel.state.value
            assert(state is AcceptTeamInvitationState.NotAuthenticated)
            assertEquals("team123", (state as AcceptTeamInvitationState.NotAuthenticated).teamId)
        }

    @Test
    fun `processInvitation when user authenticated should accept invitation and set Success`() =
        runTest(testDispatcher) {
            // Given
            val mockUser = mockk<User>()
            val team = Team(
                id = 1L,
                name = "Test Team",
                coachName = "Coach",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
            )
            every { getCurrentUserUseCase.invoke() } returns flowOf(mockUser)
            coEvery { acceptTeamInvitationUseCase.invoke("team123") } returns team

            // When
            val viewModel = createViewModel("team123")
            advanceUntilIdle()

            // Then
            assertEquals(AcceptTeamInvitationState.Success(team), viewModel.state.value)
            coVerify { acceptTeamInvitationUseCase.invoke("team123") }
        }

    @Test
    fun `processInvitation when acceptTeamInvitation throws should set Error state`() =
        runTest(testDispatcher) {
            // Given
            val mockUser = mockk<User>()
            every { getCurrentUserUseCase.invoke() } returns flowOf(mockUser)
            coEvery { acceptTeamInvitationUseCase.invoke("team123") } throws Exception("Network error")

            // When
            val viewModel = createViewModel("team123")
            advanceUntilIdle()

            // Then
            val state = viewModel.state.value
            assert(state is AcceptTeamInvitationState.Error)
            assertEquals("Network error", (state as AcceptTeamInvitationState.Error).message)
        }

    @Test
    fun `retry should call processInvitation again`() = runTest(testDispatcher) {
        // Given
        val mockUser = mockk<User>()
        val team = Team(
            id = 1L,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
        )
        every { getCurrentUserUseCase.invoke() } returns flowOf(mockUser)
        coEvery { acceptTeamInvitationUseCase.invoke("team123") } returns team
        val viewModel = createViewModel("team123")
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertEquals(AcceptTeamInvitationState.Success(team), viewModel.state.value)
        coVerify(exactly = 2) { acceptTeamInvitationUseCase.invoke("team123") }
    }
}
