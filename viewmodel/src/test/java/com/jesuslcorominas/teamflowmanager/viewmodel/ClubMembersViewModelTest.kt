package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getClubMembersUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ClubMembersViewModel(
        getClubMembers = getClubMembersUseCase,
        getUserClubMembership = getUserClubMembershipUseCase,
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
    fun `uiState should be Success with members when user has club membership`() =
        runTest(testDispatcher) {
            // Given
            val clubMember = ClubMember(
                id = 1L,
                userId = "user123",
                name = "John Doe",
                email = "john@example.com",
                clubId = 100L,
                roles = listOf("Coach"),
                firestoreId = "member1",
                clubFirestoreId = "club_fs_1",
            )
            val members = listOf(clubMember)
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(clubMember)
            every { getClubMembersUseCase.invoke("club_fs_1") } returns flowOf(members)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(ClubMembersViewModel.UiState.Success(members), viewModel.uiState.value)
        }

    @Test
    fun `uiState should be Error when exception is thrown`() = runTest(testDispatcher) {
        // Given
        every { getUserClubMembershipUseCase.invoke() } throws RuntimeException("DB error")

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(ClubMembersViewModel.UiState.Error, viewModel.uiState.value)
    }
}
