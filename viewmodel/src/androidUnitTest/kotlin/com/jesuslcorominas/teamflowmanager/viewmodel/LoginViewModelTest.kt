package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithGoogleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: LoginViewModel

    private val testUser = User(
        id = "user123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        viewModel = LoginViewModel(signInWithGoogleUseCase, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() {
        assertEquals(LoginViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `signInWithGoogle should emit Success on successful sign in`() = runTest(testDispatcher) {
        val idToken = "google_id_token"
        coEvery { signInWithGoogleUseCase(idToken) } returns Result.success(testUser)

        viewModel.signInWithGoogle(idToken)
        advanceUntilIdle()

        assertEquals(LoginViewModel.UiState.Success, viewModel.uiState.value)
        coVerify { signInWithGoogleUseCase(idToken) }
        verify { analyticsTracker.setUserId(testUser.id) }
        verify { analyticsTracker.logEvent("login", any()) }
    }

    @Test
    fun `signInWithGoogle should emit Error on failed sign in`() = runTest(testDispatcher) {
        val idToken = "google_id_token"
        val errorMessage = "Sign in failed"
        coEvery { signInWithGoogleUseCase(idToken) } returns Result.failure(Exception(errorMessage))

        viewModel.signInWithGoogle(idToken)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginViewModel.UiState.Error)
        assertEquals(errorMessage, (state as LoginViewModel.UiState.Error).message)
        coVerify { signInWithGoogleUseCase(idToken) }
        verify { analyticsTracker.logEvent("login_error", any()) }
    }

    @Test
    fun `resetState should set state back to Idle`() = runTest(testDispatcher) {
        coEvery { signInWithGoogleUseCase(any()) } returns Result.success(testUser)
        viewModel.signInWithGoogle("token")
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(LoginViewModel.UiState.Idle, viewModel.uiState.value)
    }
}
