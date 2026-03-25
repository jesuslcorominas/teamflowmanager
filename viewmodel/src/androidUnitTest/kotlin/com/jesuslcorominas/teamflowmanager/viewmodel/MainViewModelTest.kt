package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Ignore
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var hasNotificationPermissionBeenRequestedUseCase: HasNotificationPermissionBeenRequestedUseCase
    private lateinit var setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        hasNotificationPermissionBeenRequestedUseCase = mockk()
        setNotificationPermissionRequestedUseCase = mockk(relaxed = true)
        getUserClubMembershipUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MainViewModel(
        hasNotificationPermissionBeenRequestedUseCase = hasNotificationPermissionBeenRequestedUseCase,
        setNotificationPermissionRequestedUseCase = setNotificationPermissionRequestedUseCase,
        getUserClubMembership = getUserClubMembershipUseCase,
    )

    @Test
    fun `isPresident should be false when user has no club membership`() =
        runTest(testDispatcher) {
            // Given
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
            val viewModel = createViewModel()

            // When / Then
            viewModel.isPresident.test {
                // initial value from stateIn
                assertFalse(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Ignore("CLUB_HIDDEN: isPresident is always false while club feature is disabled — restore when CLUB_ORIGINAL is re-enabled")
    @Test
    fun `isPresident should be true when user has President role`() = runTest(testDispatcher) {
        // Given
        val presidentMember = ClubMember(
            id = 1L,
            userId = "user123",
            name = "John Doe",
            email = "john@example.com",
            clubId = 100L,
            roles = listOf("Presidente"),
            firestoreId = "member1",
            clubFirestoreId = "club_fs_1",
        )
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(presidentMember)
        val viewModel = createViewModel()

        // When / Then
        viewModel.isPresident.test {
            // initial value: false
            assertFalse(awaitItem())
            // computed value after upstream emits
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasNotificationPermissionBeenRequested should delegate to use case`() {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
        every { hasNotificationPermissionBeenRequestedUseCase.invoke() } returns true
        val viewModel = createViewModel()

        // When
        val result = viewModel.hasNotificationPermissionBeenRequested()

        // Then
        assertTrue(result)
        verify { hasNotificationPermissionBeenRequestedUseCase.invoke() }
    }

    @Test
    fun `setNotificationPermissionRequested should delegate to use case`() {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
        val viewModel = createViewModel()

        // When
        viewModel.setNotificationPermissionRequested(true)

        // Then
        verify { setNotificationPermissionRequestedUseCase.invoke(true) }
    }
}
