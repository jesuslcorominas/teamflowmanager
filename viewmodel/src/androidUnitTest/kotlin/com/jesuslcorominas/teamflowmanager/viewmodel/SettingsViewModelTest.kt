package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: SettingsViewModel

    private val testUser = User(
        id = "user123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        signOutUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(null)

        viewModel = SettingsViewModel(getCurrentUserUseCase, signOutUseCase, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial signOutComplete should be false`() {
        assertFalse(viewModel.signOutComplete.value)
    }

    @Test
    fun `signOut should call signOutUseCase and set signOutComplete`() = runTest(testDispatcher) {
        // Given
        coEvery { signOutUseCase() } returns Unit

        // When
        viewModel.signOut()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { signOutUseCase() }
        verify { analyticsTracker.logEvent("logout", any()) }
        verify { analyticsTracker.setUserId(null) }
        assertTrue(viewModel.signOutComplete.value)
    }

    @Test
    fun `clearSignOutComplete should reset signOutComplete to false`() = runTest(testDispatcher) {
        // Given
        coEvery { signOutUseCase() } returns Unit
        viewModel.signOut()
        advanceUntilIdle()
        assertTrue(viewModel.signOutComplete.value)

        // When
        viewModel.clearSignOutComplete()

        // Then
        assertFalse(viewModel.signOutComplete.value)
    }
}
