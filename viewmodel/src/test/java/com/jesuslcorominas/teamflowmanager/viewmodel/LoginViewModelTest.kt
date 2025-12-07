package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.HasLocalDataWithoutUserIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SignInWithGoogleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.justRun
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
    private lateinit var hasLocalDataWithoutUserIdUseCase: HasLocalDataWithoutUserIdUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mockk()
        hasLocalDataWithoutUserIdUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        viewModel = LoginViewModel(signInWithGoogleUseCase, hasLocalDataWithoutUserIdUseCase, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() {
        // Then
        assertEquals(LoginViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `signInWithGoogle should update state to Success when no local data exists`() =
        runTest {
            // Given
            val idToken = "google_id_token"
            val user = User(
                id = "user123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = "https://example.com/photo.jpg"
            )
            coEvery { signInWithGoogleUseCase(idToken) } returns Result.success(user)
            coEvery { hasLocalDataWithoutUserIdUseCase() } returns false

            // When
            viewModel.signInWithGoogle(idToken)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is LoginViewModel.UiState.Success)
            coVerify { signInWithGoogleUseCase(idToken) }
            coVerify { hasLocalDataWithoutUserIdUseCase() }
            verify { analyticsTracker.setUserId(user.id) }
            verify { analyticsTracker.logEvent("login", any()) }
        }

    @Test
    fun `signInWithGoogle should update state to NeedsMigration when local data exists`() =
        runTest {
            // Given
            val idToken = "google_id_token"
            val user = User(
                id = "user123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null
            )
            coEvery { signInWithGoogleUseCase(idToken) } returns Result.success(user)
            coEvery { hasLocalDataWithoutUserIdUseCase() } returns true

            // When
            viewModel.signInWithGoogle(idToken)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is LoginViewModel.UiState.NeedsMigration)
            coVerify { signInWithGoogleUseCase(idToken) }
            coVerify { hasLocalDataWithoutUserIdUseCase() }
            verify { analyticsTracker.setUserId(user.id) }
            verify { analyticsTracker.logEvent("login", any()) }
        }

    @Test
    fun `signInWithGoogle should update state to Success if local data check fails`() =
        runTest {
            // Given
            val idToken = "google_id_token"
            val user = User(
                id = "user123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null
            )
            coEvery { signInWithGoogleUseCase(idToken) } returns Result.success(user)
            coEvery { hasLocalDataWithoutUserIdUseCase() } throws Exception("Check failed")

            // When
            viewModel.signInWithGoogle(idToken)
            advanceUntilIdle()

            // Then
            // Should proceed with Success even if check fails
            assertTrue(viewModel.uiState.value is LoginViewModel.UiState.Success)
            coVerify { signInWithGoogleUseCase(idToken) }
            coVerify { hasLocalDataWithoutUserIdUseCase() }
        }

    @Test
    fun `signInWithGoogle should update state to Error on failed sign in`() =
        runTest {
            // Given
            val idToken = "google_id_token"
            val errorMessage = "Sign in failed"
            val exception = Exception(errorMessage)
            coEvery { signInWithGoogleUseCase(idToken) } returns Result.failure(exception)

            // When
            viewModel.signInWithGoogle(idToken)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LoginViewModel.UiState.Error)
            assertEquals(errorMessage, (state as LoginViewModel.UiState.Error).message)
            coVerify { signInWithGoogleUseCase(idToken) }
            verify { analyticsTracker.logEvent("login_error", any()) }
        }

    @Test
    fun `resetState should set state back to Idle`() = runTest {
        // Given
        val idToken = "google_id_token"
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        coEvery { signInWithGoogleUseCase(idToken) } returns Result.success(user)
        coEvery { hasLocalDataWithoutUserIdUseCase() } returns false
        viewModel.signInWithGoogle(idToken)
        advanceUntilIdle()

        // When
        viewModel.resetState()

        // Then
        assertEquals(LoginViewModel.UiState.Idle, viewModel.uiState.value)
    }
}
