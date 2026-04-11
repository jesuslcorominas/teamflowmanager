package com.jesuslcorominas.teamflowmanager.viewmodel

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateClubViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var createClubUseCase: CreateClubUseCase
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase
    private lateinit var isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: CreateClubViewModel

    private val testUser = User(
        id = "user123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        createClubUseCase = mockk()
        getCurrentUser = mockk()
        syncFcmTokenUseCase = mockk(relaxed = true)
        isNotificationPermissionGranted = mockk()
        analyticsTracker = mockk(relaxed = true)
        every { isNotificationPermissionGranted() } returns false
        every { getCurrentUser() } returns flowOf(testUser)
        viewModel = CreateClubViewModel(
            createClubUseCase = createClubUseCase,
            getCurrentUser = getCurrentUser,
            syncFcmTokenUseCase = syncFcmTokenUseCase,
            isNotificationPermissionGranted = isNotificationPermissionGranted,
            analyticsTracker = analyticsTracker,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        assertEquals("", viewModel.clubName.value)
        assertNull(viewModel.clubNameError.value)
    }

    @Test
    fun `onClubNameChanged should update club name and clear error`() = runTest {
        viewModel.onClubNameChanged("Test Club")
        advanceUntilIdle()

        assertEquals("Test Club", viewModel.clubName.value)
        assertNull(viewModel.clubNameError.value)
    }

    @Test
    fun `createClub should show error when club name is empty`() = runTest {
        viewModel.onClubNameChanged("")

        viewModel.createClub()
        advanceUntilIdle()

        assertEquals(ClubNameError.EMPTY_NAME, viewModel.clubNameError.value)
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { createClubUseCase(any()) }
    }

    @Test
    fun `createClub should show error when club name is too short`() = runTest {
        viewModel.onClubNameChanged("AB")

        viewModel.createClub()
        advanceUntilIdle()

        assertEquals(ClubNameError.NAME_TOO_SHORT, viewModel.clubNameError.value)
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { createClubUseCase(any()) }
    }

    @Test
    fun `createClub should show error when club name is too long`() = runTest {
        val longName = "A".repeat(51)
        viewModel.onClubNameChanged(longName)

        viewModel.createClub()
        advanceUntilIdle()

        assertEquals(ClubNameError.NAME_TOO_LONG, viewModel.clubNameError.value)
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { createClubUseCase(any()) }
    }

    @Test
    fun `createClub should succeed with valid club name`() = runTest {
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            remoteId = "club123",
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } returns expectedClub

        viewModel.createClub()
        advanceUntilIdle()

        assertEquals(CreateClubViewModel.UiState.Success(expectedClub), viewModel.uiState.value)
        coVerify { createClubUseCase(clubName) }
        verify {
            analyticsTracker.logEvent(
                AnalyticsEvent.CLUB_CREATED,
                mapOf(
                    AnalyticsParam.CLUB_ID to expectedClub.id.toString(),
                    AnalyticsParam.CLUB_NAME to expectedClub.name,
                ),
            )
        }
    }

    @Test
    fun `createClub should emit Loading state while creating`() = runTest(testDispatcher) {
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            remoteId = "club123",
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } coAnswers {
            kotlinx.coroutines.delay(100)
            expectedClub
        }

        viewModel.uiState.test {
            assertEquals(CreateClubViewModel.UiState.Idle, awaitItem())
            viewModel.createClub()
            assertEquals(CreateClubViewModel.UiState.Loading, awaitItem())
            assertEquals(CreateClubViewModel.UiState.Success(expectedClub), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createClub should emit Error state on failure`() = runTest {
        val clubName = "Test Club"
        val errorMessage = "Network error"
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } throws Exception(errorMessage)

        viewModel.createClub()
        advanceUntilIdle()

        assertEquals(CreateClubViewModel.UiState.Error(errorMessage), viewModel.uiState.value)
        verify {
            analyticsTracker.logEvent(
                AnalyticsEvent.CLUB_CREATION_ERROR,
                mapOf(AnalyticsParam.ERROR_MESSAGE to errorMessage),
            )
        }
    }

    @Test
    fun `createClub should trim whitespace from club name`() = runTest {
        val clubName = "  Test Club  "
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = "Test Club",
            invitationCode = "ABC12345",
            remoteId = "club123",
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase("Test Club") } returns expectedClub

        viewModel.createClub()
        advanceUntilIdle()

        coVerify { createClubUseCase("Test Club") }
        assertEquals(CreateClubViewModel.UiState.Success(expectedClub), viewModel.uiState.value)
    }

    @Test
    fun `resetState should return to Idle`() = runTest {
        val clubName = "Test Club"
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } throws Exception("Error")
        viewModel.createClub()
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `syncFcmToken is called with club remoteId on successful creation when permission is granted`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            remoteId = "club_firestore_123",
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } returns expectedClub

        viewModel.createClub()
        advanceUntilIdle()

        coVerify { syncFcmTokenUseCase("user123", "android", "club_firestore_123") }
    }

    @Test
    fun `syncFcmToken is NOT called on successful creation when permission is denied`() = runTest {
        every { isNotificationPermissionGranted() } returns false
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            remoteId = "club_firestore_123",
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } returns expectedClub

        viewModel.createClub()
        advanceUntilIdle()

        coVerify(exactly = 0) { syncFcmTokenUseCase(any(), any(), any()) }
    }
}
