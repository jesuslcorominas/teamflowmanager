package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Club

interface ClubRepository {
    /**
     * Create a new club with the current user as owner and president.
     * @param clubName The name of the club to create
     * @return The created club with generated invitation code
     */
    suspend fun createClubWithOwner(
        clubName: String,
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String,
    ): Club

    /**
     * Get a club by its invitation code.
     * @param invitationCode The invitation code to search for
     * @return The club if found, null otherwise
     */
    suspend fun getClubByInvitationCode(invitationCode: String): Club?

    suspend fun getClubById(id: String): Club?

    suspend fun regenerateInvitationCode(id: String): String

    suspend fun updateClub(
        id: String,
        name: String,
        homeGround: String?,
    ): Club
}
