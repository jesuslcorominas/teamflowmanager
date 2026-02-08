package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetUserClubMembershipUseCaseTest {
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCaseImpl

    @Before
    fun setup() {
        getCurrentUser = mockk(relaxed = true)
        clubMemberRepository = mockk(relaxed = true)
        getUserClubMembershipUseCase = GetUserClubMembershipUseCaseImpl(
            getCurrentUser = getCurrentUser,
            clubMemberRepository = clubMemberRepository
        )
    }

    @Test
    fun `invoke should return club member when user is authenticated and has club membership`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg"
        )
        val clubMember = ClubMember(
            id = 1L,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100L,
            role = "member",
            firestoreId = "member1"
        )
        every { getCurrentUser() } returns flowOf(user)
        every { clubMemberRepository.getClubMemberByUserId("user123") } returns flowOf(clubMember)

        // When
        val result = getUserClubMembershipUseCase().first()

        // Then
        assertEquals(clubMember, result)
        verify { getCurrentUser() }
        verify { clubMemberRepository.getClubMemberByUserId("user123") }
    }

    @Test
    fun `invoke should return null when user is authenticated but has no club membership`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg"
        )
        every { getCurrentUser() } returns flowOf(user)
        every { clubMemberRepository.getClubMemberByUserId("user123") } returns flowOf(null)

        // When
        val result = getUserClubMembershipUseCase().first()

        // Then
        assertNull(result)
        verify { getCurrentUser() }
        verify { clubMemberRepository.getClubMemberByUserId("user123") }
    }

    @Test
    fun `invoke should return null when user is not authenticated`() = runTest {
        // Given
        every { getCurrentUser() } returns flowOf(null)

        // When
        val result = getUserClubMembershipUseCase().first()

        // Then
        assertNull(result)
        verify { getCurrentUser() }
        verify(exactly = 0) { clubMemberRepository.getClubMemberByUserId(any()) }
    }
}
