package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClubSelectionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var viewModel: ClubSelectionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signOutUseCase = mockk(relaxed = true)
        viewModel = ClubSelectionViewModel(signOutUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `givenNoAction_thenSignOutIsNotComplete`() {
        assertFalse(viewModel.signOutComplete.value)
    }

    @Test
    fun `whenSignOut_thenCallsUseCaseAndCompletes`() =
        runTest(testDispatcher) {
            // When
            viewModel.signOut()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { signOutUseCase() }
            assertTrue(viewModel.signOutComplete.value)
        }

    @Test
    fun `givenSignOutFails_whenSignOut_thenStillCompletes`() =
        runTest(testDispatcher) {
            // Given — the user must never be stuck on this screen
            coEvery { signOutUseCase() } throws IllegalStateException("network")

            // When
            viewModel.signOut()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.signOutComplete.value)
        }

    @Test
    fun `givenSignOutComplete_whenClearSignOutComplete_thenResets`() =
        runTest(testDispatcher) {
            // Given
            viewModel.signOut()
            advanceUntilIdle()

            // When
            viewModel.clearSignOutComplete()

            // Then
            assertFalse(viewModel.signOutComplete.value)
        }
}
