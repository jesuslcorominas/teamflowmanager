package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ClubRepositoryImplTest {

    private lateinit var clubDataSource: ClubDataSource
    private lateinit var repository: ClubRepositoryImpl

    @Before
    fun setup() {
        clubDataSource = mockk(relaxed = true)
        repository = ClubRepositoryImpl(clubDataSource)
    }

    private fun createClub(
        id: Long = 1L,
        ownerId: String = "owner-123",
        name: String = "Test Club",
        invitationCode: String = "ABCD1234",
        firestoreId: String? = "club-firestore-id",
    ) = Club(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        firestoreId = firestoreId,
    )

    // --- createClubWithOwner ---

    @Test
    fun `givenClubDetails_whenCreateClubWithOwner_thenReturnsCreatedClub`() = runTest {
        val clubName = "My Club"
        val userId = "user-abc"
        val userName = "John Doe"
        val userEmail = "john@example.com"
        val expectedClub = createClub(name = clubName, ownerId = userId)
        coEvery {
            clubDataSource.createClubWithOwner(clubName, userId, userName, userEmail)
        } returns expectedClub

        val result = repository.createClubWithOwner(clubName, userId, userName, userEmail)

        assertEquals(expectedClub, result)
        coVerify { clubDataSource.createClubWithOwner(clubName, userId, userName, userEmail) }
    }

    // --- getClubByInvitationCode ---

    @Test
    fun `givenValidInvitationCode_whenGetClubByInvitationCode_thenReturnsClub`() = runTest {
        val invitationCode = "ABCD1234"
        val club = createClub(invitationCode = invitationCode)
        coEvery { clubDataSource.getClubByInvitationCode(invitationCode) } returns club

        val result = repository.getClubByInvitationCode(invitationCode)

        assertEquals(club, result)
        coVerify { clubDataSource.getClubByInvitationCode(invitationCode) }
    }

    @Test
    fun `givenInvalidInvitationCode_whenGetClubByInvitationCode_thenReturnsNull`() = runTest {
        val invitationCode = "INVALID99"
        coEvery { clubDataSource.getClubByInvitationCode(invitationCode) } returns null

        val result = repository.getClubByInvitationCode(invitationCode)

        assertNull(result)
    }
}
