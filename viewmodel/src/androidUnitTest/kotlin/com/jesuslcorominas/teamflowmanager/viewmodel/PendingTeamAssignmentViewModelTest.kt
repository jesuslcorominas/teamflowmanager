package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class PendingTeamAssignmentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val team = MutableStateFlow<Team?>(null)
    private lateinit var getTeam: GetTeamUseCase
    private lateinit var signOut: SignOutUseCase
    private lateinit var viewModel: PendingTeamAssignmentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeam = mockk()
        every { getTeam() } returns team
        signOut = mockk(relaxed = true)
        viewModel = PendingTeamAssignmentViewModel(getTeam, signOut)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `givenNoTeam_thenWaiting`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            assertEquals(PendingTeamAssignmentViewModel.UiState.Waiting, viewModel.uiState.value)
        }

    @Test
    fun `givenATeamIsAssigned_thenTeamAssigned`() =
        runTest(testDispatcher) {
            // When
            team.value = mockk(relaxed = true)
            advanceUntilIdle()

            // Then
            assertEquals(PendingTeamAssignmentViewModel.UiState.TeamAssigned, viewModel.uiState.value)
        }

    @Test
    fun `whenSignOut_thenCallsUseCaseAndSignedOut`() =
        runTest(testDispatcher) {
            // When
            viewModel.signOut()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { signOut() }
            assertEquals(PendingTeamAssignmentViewModel.UiState.SignedOut, viewModel.uiState.value)
        }

    @Test
    fun `givenSignOutFails_whenSignOut_thenStillSignedOut`() =
        runTest(testDispatcher) {
            // Given
            coEvery { signOut() } throws IllegalStateException("network")

            // When
            viewModel.signOut()
            advanceUntilIdle()

            // Then
            assertEquals(PendingTeamAssignmentViewModel.UiState.SignedOut, viewModel.uiState.value)
        }
}
