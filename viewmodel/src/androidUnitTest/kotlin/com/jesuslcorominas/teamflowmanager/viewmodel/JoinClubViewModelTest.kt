package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubResult
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinClubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var joinClubByCodeUseCase: JoinClubByCodeUseCase
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase
    private lateinit var isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: JoinClubViewModel

    private val testUser = User(
        id = "user123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        joinClubByCodeUseCase = mockk()
        getCurrentUser = mockk()
        syncFcmTokenUseCase = mockk(relaxed = true)
        isNotificationPermissionGranted = mockk()
        analyticsTracker = mockk(relaxed = true)
        every { isNotificationPermissionGranted() } returns false
        every { getCurrentUser() } returns flowOf(testUser)
        viewModel = JoinClubViewModel(
            joinClubByCodeUseCase = joinClubByCodeUseCase,
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
    fun `initial state should be Idle with empty code and no error`() = runTest {
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
        assertEquals("", viewModel.invitationCode.value)
        assertNull(viewModel.invitationCodeError.value)
    }

    @Test
    fun `onInvitationCodeChanged should filter non-alphanumeric chars and uppercase`() = runTest {
        viewModel.onInvitationCodeChanged("abc-123!@#")
        assertEquals("ABC123", viewModel.invitationCode.value)
        assertNull(viewModel.invitationCodeError.value)
    }

    @Test
    fun `joinClub should set EMPTY_CODE error when code is empty`() = runTest {
        viewModel.onInvitationCodeChanged("")

        viewModel.joinClub()
        advanceUntilIdle()

        assertEquals(InvitationCodeError.EMPTY_CODE, viewModel.invitationCodeError.value)
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `joinClub should set CODE_TOO_SHORT error when code is too short`() = runTest {
        viewModel.onInvitationCodeChanged("AB")

        viewModel.joinClub()
        advanceUntilIdle()

        assertEquals(InvitationCodeError.CODE_TOO_SHORT, viewModel.invitationCodeError.value)
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `joinClub should set CODE_TOO_LONG error when code exceeds max length`() = runTest {
        viewModel.onInvitationCodeChanged("A".repeat(11))

        viewModel.joinClub()
        advanceUntilIdle()

        assertEquals(InvitationCodeError.CODE_TOO_LONG, viewModel.invitationCodeError.value)
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `joinClub should succeed with valid code`() = runTest {
        val code = "ABC123"
        val club = Club(id = 1L, ownerId = "owner", name = "Test Club", invitationCode = code)
        val clubMember = ClubMember(
            id = 1L,
            userId = "user1",
            name = "User",
            email = "user@test.com",
            clubId = 1L,
            roles = listOf("Coach"),
        )
        val result = JoinClubResult(club = club, orphanTeam = null, clubMember = clubMember)
        coEvery { joinClubByCodeUseCase.invoke(code) } returns result
        viewModel.onInvitationCodeChanged(code)

        viewModel.joinClub()
        advanceUntilIdle()

        assertEquals(JoinClubViewModel.UiState.Success(result), viewModel.uiState.value)
        coVerify { joinClubByCodeUseCase.invoke(code) }
    }

    @Test
    fun `joinClub should track orphan team event when result has orphan team`() = runTest {
        val code = "ABC123"
        val club = Club(id = 1L, ownerId = "owner", name = "Test Club", invitationCode = code)
        val orphanTeam = Team(
            id = 5L,
            name = "Orphan Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
        )
        val clubMember = ClubMember(
            id = 1L,
            userId = "user1",
            name = "User",
            email = "user@test.com",
            clubId = 1L,
            roles = listOf("Coach"),
        )
        val result = JoinClubResult(club = club, orphanTeam = orphanTeam, clubMember = clubMember)
        coEvery { joinClubByCodeUseCase.invoke(code) } returns result
        viewModel.onInvitationCodeChanged(code)

        viewModel.joinClub()
        advanceUntilIdle()

        assertEquals(JoinClubViewModel.UiState.Success(result), viewModel.uiState.value)
    }

    @Test
    fun `joinClub should set Error state on exception`() = runTest {
        val code = "ABC123"
        coEvery { joinClubByCodeUseCase.invoke(code) } throws Exception("Network error")
        viewModel.onInvitationCodeChanged(code)

        viewModel.joinClub()
        advanceUntilIdle()

        assertEquals(JoinClubViewModel.UiState.Error("Network error"), viewModel.uiState.value)
    }

    @Test
    fun `resetState should return to Idle`() = runTest {
        val code = "ABC123"
        coEvery { joinClubByCodeUseCase.invoke(code) } throws Exception("Error")
        viewModel.onInvitationCodeChanged(code)
        viewModel.joinClub()
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `syncFcmToken is called with club remoteId on successful join when permission is granted`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        val code = "ABC123"
        val club = Club(
            id = 1L,
            ownerId = "owner",
            name = "Test Club",
            invitationCode = code,
            remoteId = "club_firestore_123",
        )
        val clubMember = ClubMember(
            id = 1L,
            userId = "user1",
            name = "User",
            email = "user@test.com",
            clubId = 1L,
            roles = listOf("Coach"),
        )
        val result = JoinClubResult(club = club, orphanTeam = null, clubMember = clubMember)
        coEvery { joinClubByCodeUseCase.invoke(code) } returns result
        viewModel.onInvitationCodeChanged(code)

        viewModel.joinClub()
        advanceUntilIdle()

        coVerify { syncFcmTokenUseCase("user123", "android", "club_firestore_123") }
    }

    @Test
    fun `syncFcmToken is NOT called on successful join when permission is denied`() = runTest {
        every { isNotificationPermissionGranted() } returns false
        val code = "ABC123"
        val club = Club(
            id = 1L,
            ownerId = "owner",
            name = "Test Club",
            invitationCode = code,
            remoteId = "club_firestore_123",
        )
        val clubMember = ClubMember(
            id = 1L,
            userId = "user1",
            name = "User",
            email = "user@test.com",
            clubId = 1L,
            roles = listOf("Coach"),
        )
        val result = JoinClubResult(club = club, orphanTeam = null, clubMember = clubMember)
        coEvery { joinClubByCodeUseCase.invoke(code) } returns result
        viewModel.onInvitationCodeChanged(code)

        viewModel.joinClub()
        advanceUntilIdle()

        coVerify(exactly = 0) { syncFcmTokenUseCase(any(), any(), any()) }
    }
}
