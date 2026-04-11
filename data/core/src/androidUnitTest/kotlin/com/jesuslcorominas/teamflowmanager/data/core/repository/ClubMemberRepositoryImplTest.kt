package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ClubMemberRepositoryImplTest {

    private lateinit var clubMemberDataSource: ClubMemberDataSource
    private lateinit var repository: ClubMemberRepositoryImpl

    @Before
    fun setup() {
        clubMemberDataSource = mockk(relaxed = true)
        repository = ClubMemberRepositoryImpl(clubMemberDataSource)
    }

    private fun createClubMember(
        id: Long = 1L,
        userId: String = "user-123",
        name: String = "John Doe",
        email: String = "john@example.com",
        clubId: Long = 10L,
        roles: List<String> = listOf("player"),
        remoteId: String? = "member-firestore-id",
        clubRemoteId: String? = "club-firestore-id",
    ) = ClubMember(
        id = id,
        userId = userId,
        name = name,
        email = email,
        clubId = clubId,
        roles = roles,
        remoteId = remoteId,
        clubRemoteId = clubRemoteId,
    )

    // --- getClubMemberByUserId ---

    @Test
    fun `givenExistingUserId_whenGetClubMemberByUserId_thenReturnsMember`() = runTest {
        val userId = "user-123"
        val member = createClubMember(userId = userId)
        every { clubMemberDataSource.getClubMemberByUserId(userId) } returns flowOf(member)

        val result = repository.getClubMemberByUserId(userId).first()

        assertEquals(member, result)
    }

    @Test
    fun `givenUnknownUserId_whenGetClubMemberByUserId_thenReturnsNull`() = runTest {
        val userId = "unknown-user"
        every { clubMemberDataSource.getClubMemberByUserId(userId) } returns flowOf(null)

        val result = repository.getClubMemberByUserId(userId).first()

        assertNull(result)
    }

    // --- getClubMembers ---

    @Test
    fun `givenClubFirestoreId_whenGetClubMembers_thenReturnsAllMembers`() = runTest {
        val clubRemoteId = "club-123"
        val members = listOf(
            createClubMember(id = 1L, userId = "user-1"),
            createClubMember(id = 2L, userId = "user-2"),
        )
        every { clubMemberDataSource.getClubMembers(clubRemoteId) } returns flowOf(members)

        val result = repository.getClubMembers(clubRemoteId).first()

        assertEquals(members, result)
    }

    @Test
    fun `givenClubWithNoMembers_whenGetClubMembers_thenReturnsEmptyList`() = runTest {
        val clubRemoteId = "empty-club"
        every { clubMemberDataSource.getClubMembers(clubRemoteId) } returns flowOf(emptyList())

        val result = repository.getClubMembers(clubRemoteId).first()

        assertEquals(emptyList<ClubMember>(), result)
    }

    // --- createOrUpdateClubMember ---

    @Test
    fun `givenMemberDetails_whenCreateOrUpdateClubMember_thenReturnsSavedMember`() = runTest {
        val userId = "user-123"
        val name = "John Doe"
        val email = "john@example.com"
        val clubId = 10L
        val clubRemoteId = "club-firestore-id"
        val roles = listOf("player")
        val expectedMember = createClubMember(userId = userId, roles = roles)
        coEvery {
            clubMemberDataSource.createOrUpdateClubMember(userId, name, email, clubId, clubRemoteId, roles)
        } returns expectedMember

        val result = repository.createOrUpdateClubMember(userId, name, email, clubId, clubRemoteId, roles)

        assertEquals(expectedMember, result)
        coVerify { clubMemberDataSource.createOrUpdateClubMember(userId, name, email, clubId, clubRemoteId, roles) }
    }

    @Test
    fun `givenMemberWithMultipleRoles_whenCreateOrUpdateClubMember_thenReturnsMemberWithRoles`() = runTest {
        val userId = "president-user"
        val roles = listOf("president", "coach")
        val expectedMember = createClubMember(userId = userId, roles = roles)
        coEvery {
            clubMemberDataSource.createOrUpdateClubMember(any(), any(), any(), any(), any(), roles)
        } returns expectedMember

        val result = repository.createOrUpdateClubMember(userId, "Name", "email@test.com", 1L, "club-id", roles)

        assertEquals(roles, result.roles)
    }

    // --- updateClubMemberRoles ---

    @Test
    fun `givenUserAndNewRoles_whenUpdateClubMemberRoles_thenDelegatesToDataSource`() = runTest {
        val userId = "user-123"
        val clubRemoteId = "club-firestore-id"
        val roles = listOf("coach")

        repository.updateClubMemberRoles(userId, clubRemoteId, roles)

        coVerify { clubMemberDataSource.updateClubMemberRoles(userId, clubRemoteId, roles) }
    }

    @Test
    fun `givenEmptyRolesList_whenUpdateClubMemberRoles_thenDelegatesToDataSource`() = runTest {
        val userId = "user-123"
        val clubRemoteId = "club-firestore-id"

        repository.updateClubMemberRoles(userId, clubRemoteId, emptyList())

        coVerify { clubMemberDataSource.updateClubMemberRoles(userId, clubRemoteId, emptyList()) }
    }

    // --- addClubMemberRole ---

    @Test
    fun `givenUserAndRole_whenAddClubMemberRole_thenDelegatesToDataSource`() = runTest {
        val userId = "user-123"
        val clubRemoteId = "club-firestore-id"
        val role = "president"

        repository.addClubMemberRole(userId, clubRemoteId, role)

        coVerify { clubMemberDataSource.addClubMemberRole(userId, clubRemoteId, role) }
    }

    // --- getClubMemberByUserIdAndClub ---

    @Test
    fun `givenUserIdAndClubFirestoreId_whenGetClubMemberByUserIdAndClub_thenReturnsMember`() = runTest {
        val userId = "user-123"
        val clubRemoteId = "club-firestore-id"
        val member = createClubMember(userId = userId, clubRemoteId = clubRemoteId)
        coEvery { clubMemberDataSource.getClubMemberByUserIdAndClub(userId, clubRemoteId) } returns member

        val result = repository.getClubMemberByUserIdAndClub(userId, clubRemoteId)

        assertEquals(member, result)
        coVerify { clubMemberDataSource.getClubMemberByUserIdAndClub(userId, clubRemoteId) }
    }

    @Test
    fun `givenNonMemberUserId_whenGetClubMemberByUserIdAndClub_thenReturnsNull`() = runTest {
        val userId = "non-member-user"
        val clubRemoteId = "club-firestore-id"
        coEvery { clubMemberDataSource.getClubMemberByUserIdAndClub(userId, clubRemoteId) } returns null

        val result = repository.getClubMemberByUserIdAndClub(userId, clubRemoteId)

        assertNull(result)
    }
}
