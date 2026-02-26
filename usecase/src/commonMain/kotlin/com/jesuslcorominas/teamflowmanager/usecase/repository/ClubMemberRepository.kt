package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.coroutines.flow.Flow

interface ClubMemberRepository {
    /**
     * Get club member by user ID.
     * @param userId The Firebase user ID
     * @return Flow emitting the club member if found, null otherwise
     */
    fun getClubMemberByUserId(userId: String): Flow<ClubMember?>

    /**
     * Get all members of a club.
     * @param clubFirestoreId The Firestore document ID of the club
     * @return Flow emitting list of club members
     */
    fun getClubMembers(clubFirestoreId: String): Flow<List<ClubMember>>

    /**
     * Create or update a club member.
     * @param userId The Firebase user ID
     * @param name The user's display name
     * @param email The user's email
     * @param clubId The club's numeric ID
     * @param clubFirestoreId The club's Firestore document ID
     * @param roles The member's roles in the club
     * @return The created or updated ClubMember
     */
    suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubId: Long,
        clubFirestoreId: String,
        roles: List<String>
    ): ClubMember

    /**
     * Update the roles of a club member (replaces existing roles).
     * @param userId The Firebase user ID
     * @param clubFirestoreId The club's Firestore document ID
     * @param roles The new roles for the member
     */
    suspend fun updateClubMemberRoles(
        userId: String,
        clubFirestoreId: String,
        roles: List<String>
    )

    /**
     * Add a role to a club member's existing roles.
     * @param userId The Firebase user ID
     * @param clubFirestoreId The club's Firestore document ID
     * @param role The role to add
     */
    suspend fun addClubMemberRole(
        userId: String,
        clubFirestoreId: String,
        role: String
    )

    /**
     * Get club member by user ID and club ID.
     * @param userId The Firebase user ID
     * @param clubFirestoreId The club's Firestore document ID
     * @return The club member if found, null otherwise
     */
    suspend fun getClubMemberByUserIdAndClub(
        userId: String,
        clubFirestoreId: String
    ): ClubMember?
}
