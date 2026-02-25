package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubResult
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinClubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var joinClubByCodeUseCase: JoinClubByCodeUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: JoinClubViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        joinClubByCodeUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        viewModel = JoinClubViewModel(joinClubByCodeUseCase, analyticsTracker)
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
        // Given
        viewModel.onInvitationCodeChanged("")

        // When
        viewModel.joinClub()
        advanceUntilIdle()

        // Then
        assertEquals(InvitationCodeError.EMPTY_CODE, viewModel.invitationCodeError.value)
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `joinClub should set CODE_TOO_SHORT error when code is too short`() = runTest {
        // Given
        viewModel.onInvitationCodeChanged("AB")

        // When
        viewModel.joinClub()
        advanceUntilIdle()

        // Then
        assertEquals(InvitationCodeError.CODE_TOO_SHORT, viewModel.invitationCodeError.value)
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `joinClub should set CODE_TOO_LONG error when code exceeds max length`() = runTest {
        // Given
        viewModel.onInvitationCodeChanged("A".repeat(11))

        // When
        viewModel.joinClub()
        advanceUntilIdle()

        // Then
        assertEquals(InvitationCodeError.CODE_TOO_LONG, viewModel.invitationCodeError.value)
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `joinClub should succeed with valid code`() = runTest {
        // Given
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

        // When
        viewModel.joinClub()
        advanceUntilIdle()

        // Then
        assertEquals(JoinClubViewModel.UiState.Success(result), viewModel.uiState.value)
        coVerify { joinClubByCodeUseCase.invoke(code) }
    }

    @Test
    fun `joinClub should track orphan team event when result has orphan team`() = runTest {
        // Given
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

        // When
        viewModel.joinClub()
        advanceUntilIdle()

        // Then
        assertEquals(JoinClubViewModel.UiState.Success(result), viewModel.uiState.value)
    }

    @Test
    fun `joinClub should set Error state on exception`() = runTest {
        // Given
        val code = "ABC123"
        coEvery { joinClubByCodeUseCase.invoke(code) } throws Exception("Network error")
        viewModel.onInvitationCodeChanged(code)

        // When
        viewModel.joinClub()
        advanceUntilIdle()

        // Then
        assertEquals(JoinClubViewModel.UiState.Error("Network error"), viewModel.uiState.value)
    }

    @Test
    fun `resetState should return to Idle`() = runTest {
        // Given
        val code = "ABC123"
        coEvery { joinClubByCodeUseCase.invoke(code) } throws Exception("Error")
        viewModel.onInvitationCodeChanged(code)
        viewModel.joinClub()
        advanceUntilIdle()

        // When
        viewModel.resetState()

        // Then
        assertEquals(JoinClubViewModel.UiState.Idle, viewModel.uiState.value)
    }
}
