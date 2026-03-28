package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
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
    private lateinit var deleteFcmTokenUseCase: DeleteFcmTokenUseCase
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
        deleteFcmTokenUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(null)

        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
        )
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
        coEvery { signOutUseCase() } returns Unit

        viewModel.signOut()
        advanceUntilIdle()

        coVerify(exactly = 1) { signOutUseCase() }
        verify { analyticsTracker.logEvent("logout", any()) }
        verify { analyticsTracker.setUserId(null) }
        assertTrue(viewModel.signOutComplete.value)
    }

    @Test
    fun `clearSignOutComplete should reset signOutComplete to false`() = runTest(testDispatcher) {
        coEvery { signOutUseCase() } returns Unit
        viewModel.signOut()
        advanceUntilIdle()
        assertTrue(viewModel.signOutComplete.value)

        viewModel.clearSignOutComplete()

        assertFalse(viewModel.signOutComplete.value)
    }

    @Test
    fun `signOut should call deleteFcmTokenUseCase with userId when user is logged in`() = runTest(testDispatcher) {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
        )
        advanceUntilIdle()
        coEvery { signOutUseCase() } returns Unit

        viewModel.signOut()
        advanceUntilIdle()

        coVerify { deleteFcmTokenUseCase("user123") }
        coVerify { signOutUseCase() }
    }

    @Test
    fun `signOut should NOT call deleteFcmTokenUseCase when no user is logged in`() = runTest(testDispatcher) {
        coEvery { signOutUseCase() } returns Unit

        viewModel.signOut()
        advanceUntilIdle()

        coVerify(exactly = 0) { deleteFcmTokenUseCase(any()) }
        coVerify { signOutUseCase() }
    }
}
