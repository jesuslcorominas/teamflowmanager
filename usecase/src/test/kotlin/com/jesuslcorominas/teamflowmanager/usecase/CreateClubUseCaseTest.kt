package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateClubUseCaseTest {
    private lateinit var clubRepository: ClubRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var getTeam: GetTeamUseCase
    private lateinit var updateTeam: UpdateTeamUseCase
    private lateinit var createClubUseCase: CreateClubUseCaseImpl

    @Before
    fun setup() {
        clubRepository = mockk(relaxed = true)
        getCurrentUser = mockk(relaxed = true)
        getTeam = mockk(relaxed = true)
        updateTeam = mockk(relaxed = true)
        createClubUseCase = CreateClubUseCaseImpl(
            clubRepository = clubRepository,
            getCurrentUser = getCurrentUser,
            getTeam = getTeam,
            updateTeam = updateTeam
        )
    }

    @Test
    fun `invoke should create club when user is authenticated with valid data`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg"
        )
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            firestoreId = "club123"
        )
        every { getCurrentUser() } returns flowOf(user)
        every { getTeam() } returns flowOf(null)
        coEvery {
            clubRepository.createClubWithOwner(
                clubName = clubName,
                currentUserId = "user123",
                currentUserName = "Test User",
                currentUserEmail = "test@example.com"
            )
        } returns expectedClub

        // When
        val result = createClubUseCase(clubName)

        // Then
        assertEquals(expectedClub, result)
        coVerify {
            clubRepository.createClubWithOwner(
                clubName = clubName,
                currentUserId = "user123",
                currentUserName = "Test User",
                currentUserEmail = "test@example.com"
            )
        }
        coVerify(exactly = 0) { updateTeam(any()) }
    }

    @Test
    fun `invoke should create club and associate existing team`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg"
        )
        val existingTeam = Team(
            id = 1L,
            name = "Test Team",
            coachName = "Test User",
            delegateName = "Delegate",
            teamType = TeamType.MALE,
            coachId = "user123",
            clubId = null,
            clubFirestoreId = null
        )
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            firestoreId = "club123"
        )
        every { getCurrentUser() } returns flowOf(user)
        every { getTeam() } returns flowOf(existingTeam)
        coEvery {
            clubRepository.createClubWithOwner(
                clubName = clubName,
                currentUserId = "user123",
                currentUserName = "Test User",
                currentUserEmail = "test@example.com"
            )
        } returns expectedClub
        coEvery { updateTeam(any()) } returns Unit

        // When
        val result = createClubUseCase(clubName)

        // Then
        assertEquals(expectedClub, result)
        coVerify {
            updateTeam(
                existingTeam.copy(clubFirestoreId = "club123")
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke should throw exception when user is not authenticated`() = runTest {
        // Given
        every { getCurrentUser() } returns flowOf(null)
        every { getTeam() } returns flowOf(null)

        // When
        createClubUseCase("Test Club")

        // Then - exception is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke should throw exception when user has no display name`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = null,
            photoUrl = null
        )
        every { getCurrentUser() } returns flowOf(user)
        every { getTeam() } returns flowOf(null)

        // When
        createClubUseCase("Test Club")

        // Then - exception is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke should throw exception when user has no email`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = null,
            displayName = "Test User",
            photoUrl = null
        )
        every { getCurrentUser() } returns flowOf(user)
        every { getTeam() } returns flowOf(null)

        // When
        createClubUseCase("Test Club")

        // Then - exception is thrown
    }
}
