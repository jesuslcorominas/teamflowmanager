package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var analyticsTracker: AnalyticsTracker

    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        signOutUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(User("1", "test@test.com", "Test User", null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = SettingsViewModel(
            getCurrentUserUseCase,
            signOutUseCase,
            analyticsTracker
        )
    }

    @Test
    fun `currentUser should expose flow from use case`() = runTest {
        // Given
        val user = User("1", "test@test.com", "Test User", null)
        every { getCurrentUserUseCase() } returns flowOf(user)

        // When
        createViewModel()

        // Then
        viewModel.currentUser.test {
            assertEquals(null, awaitItem())
            assertEquals(user, awaitItem())
        }
    }

    @Test
    fun `signOut should call use case and update state`() = runTest {
        // Given
        coEvery { signOutUseCase() } returns Unit
        createViewModel()

        // When
        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.signOutComplete.value)
        coVerify { signOutUseCase() }
        coVerify { analyticsTracker.logEvent("logout", any()) }
        coVerify { analyticsTracker.setUserId(null) }
    }

    @Test
    fun `clearSignOutComplete should reset state`() = runTest {
        // Given
        coEvery { signOutUseCase() } returns Unit
        createViewModel()
        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.signOutComplete.value)

        // When
        viewModel.clearSignOutComplete()

        // Then
        assertFalse(viewModel.signOutComplete.value)
    }
}
