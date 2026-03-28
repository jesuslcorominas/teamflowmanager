package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.coroutines.flow.Flow

interface ClubMemberDataSource {
    /**
     * Get club member by user ID.
     * @param userId The Firebase user ID
     * @return Flow emitting the club member if found, null otherwise
     */
    fun getClubMemberByUserId(userId: String): Flow<ClubMember?>

    /**
     * Get all members of a club.
     * @param clubId The identifier of the club
     * @return Flow emitting list of club members
     */
    fun getClubMembers(clubId: String): Flow<List<ClubMember>>

    /**
     * Create or update a club member.
     * @param userId The Firebase user ID
     * @param name The user's display name
     * @param email The user's email
     * @param clubNumericId The club's local numeric ID
     * @param clubId The club's string identifier
     * @param roles The member's roles in the club
     * @return The created or updated ClubMember
     */
    suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubNumericId: Long,
        clubId: String,
        roles: List<String>,
    ): ClubMember

    /**
     * Update the roles of a club member (replaces existing roles).
     * @param userId The Firebase user ID
     * @param clubId The identifier of the club
     * @param roles The new roles for the member
     */
    suspend fun updateClubMemberRoles(
        userId: String,
        clubId: String,
        roles: List<String>,
    )

    /**
     * Add a role to a club member's existing roles.
     * @param userId The Firebase user ID
     * @param clubId The identifier of the club
     * @param role The role to add
     */
    suspend fun addClubMemberRole(
        userId: String,
        clubId: String,
        role: String,
    )

    /**
     * Get club member by user ID and club ID.
     * @param userId The Firebase user ID
     * @param clubId The identifier of the club
     * @return The club member if found, null otherwise
     */
    suspend fun getClubMemberByUserIdAndClub(
        userId: String,
        clubId: String,
    ): ClubMember?
}
