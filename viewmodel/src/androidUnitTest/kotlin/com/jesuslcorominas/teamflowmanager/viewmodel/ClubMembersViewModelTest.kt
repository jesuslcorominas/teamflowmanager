package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemoveClubMemberUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClubMembersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getClubMembersUseCase: GetClubMembersUseCase
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase
    private lateinit var removeClubMemberUseCase: RemoveClubMemberUseCase

    private val presidentMember = ClubMember(
        id = 1L,
        userId = "user123",
        name = "President User",
        email = "president@example.com",
        clubId = 100L,
        roles = listOf("Presidente"),
        remoteId = "member1",
        clubRemoteId = "club_fs_1",
    )

    private val coachMember = ClubMember(
        id = 2L,
        userId = "coach456",
        name = "Coach User",
        email = "coach@example.com",
        clubId = 100L,
        roles = listOf("Coach"),
        remoteId = "member2",
        clubRemoteId = "club_fs_1",
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getClubMembersUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
        removeClubMemberUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ClubMembersViewModel(
        getClubMembers = getClubMembersUseCase,
        getUserClubMembership = getUserClubMembershipUseCase,
        removeClubMember = removeClubMemberUseCase,
    )

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals(ClubMembersViewModel.UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be NoClubMembership when user has no membership`() =
        runTest(testDispatcher) {
            // Given
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(ClubMembersViewModel.UiState.NoClubMembership, viewModel.uiState.value)
        }

    @Test
    fun `uiState should be Success with members and president flag true for president`() =
        runTest(testDispatcher) {
            // Given
            val members = listOf(presidentMember, coachMember)
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(presidentMember)
            every { getClubMembersUseCase.invoke("club_fs_1") } returns flowOf(members)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(
                ClubMembersViewModel.UiState.Success(
                    members = members,
                    currentUserId = "user123",
                    currentUserIsPresident = true,
                    clubRemoteId = "club_fs_1",
                ),
                viewModel.uiState.value,
            )
        }

    @Test
    fun `uiState should have currentUserIsPresident false for non-president`() =
        runTest(testDispatcher) {
            // Given
            val members = listOf(coachMember)
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(coachMember)
            every { getClubMembersUseCase.invoke("club_fs_1") } returns flowOf(members)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as ClubMembersViewModel.UiState.Success
            assertEquals(false, state.currentUserIsPresident)
        }

    @Test
    fun `expelMember should call removeClubMember use case`() =
        runTest(testDispatcher) {
            // Given
            val members = listOf(presidentMember, coachMember)
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(presidentMember)
            every { getClubMembersUseCase.invoke("club_fs_1") } returns flowOf(members)
            coEvery { removeClubMemberUseCase.invoke("coach456", "club_fs_1") } returns Unit

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.expelMember("coach456", "club_fs_1")
            advanceUntilIdle()

            // Then
            coVerify { removeClubMemberUseCase.invoke("coach456", "club_fs_1") }
        }

    @Test
    fun `expelMember should set Error state when use case throws`() =
        runTest(testDispatcher) {
            // Given
            val members = listOf(presidentMember, coachMember)
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(presidentMember)
            every { getClubMembersUseCase.invoke("club_fs_1") } returns flowOf(members)
            coEvery {
                removeClubMemberUseCase.invoke(any(), any())
            } throws RuntimeException("network error")

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.expelMember("coach456", "club_fs_1")
            advanceUntilIdle()

            // Then
            assertEquals(ClubMembersViewModel.UiState.Error, viewModel.uiState.value)
        }

    @Test
    fun `uiState should be Error when exception is thrown loading members`() =
        runTest(testDispatcher) {
            // Given
            every { getUserClubMembershipUseCase.invoke() } throws RuntimeException("DB error")

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(ClubMembersViewModel.UiState.Error, viewModel.uiState.value)
        }
}