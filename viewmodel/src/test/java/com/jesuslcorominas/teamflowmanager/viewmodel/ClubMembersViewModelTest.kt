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

    private lateinit var getClubMembers: GetClubMembersUseCase
    private lateinit var getUserClubMembership: GetUserClubMembershipUseCase

    private lateinit var viewModel: ClubMembersViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getClubMembers = mockk()
        getUserClubMembership = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = ClubMembersViewModel(getClubMembers, getUserClubMembership)
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getUserClubMembership() } returns flowOf(null)

        // When
        createViewModel()

        // Then
        assertEquals(ClubMembersViewModel.UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when members are loaded`() = runTest {
        // Given
        val clubMember = ClubMember(1L, "user1", "Name", "email", 100L, "Presidente", "member1", "club1")
        val members = listOf(clubMember)
        every { getUserClubMembership() } returns flowOf(clubMember)
        every { getClubMembers("club1") } returns flowOf(members)

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(ClubMembersViewModel.UiState.Success(members), state)
    }

    @Test
    fun `uiState should be NoClubMembership when user has no club`() = runTest {
        // Given
        every { getUserClubMembership() } returns flowOf(null)

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(ClubMembersViewModel.UiState.NoClubMembership, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Error when use case throws`() = runTest {
        // Given
        val clubMember = ClubMember(1L, "user1", "Name", "email", 100L, "Presidente", "member1", "club1")
        every { getUserClubMembership() } returns flowOf(clubMember)
        every { getClubMembers("club1") } throws Exception("Error")

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(ClubMembersViewModel.UiState.Error, viewModel.uiState.value)
    }
}
