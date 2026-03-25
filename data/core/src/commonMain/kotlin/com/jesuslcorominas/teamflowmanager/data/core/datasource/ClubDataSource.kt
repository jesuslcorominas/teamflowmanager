package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Club

interface ClubDataSource {
    /**
     * Create a new club with the current user as owner and president.
     * This operation creates the club first, then the clubMember sequentially,
     * to work with Firestore security rules that require the club to exist
     * before creating the clubMember.
     *
     * @param clubName The name of the club to create
     * @param currentUserId The user ID of the club owner
     * @param currentUserName The display name of the club owner
     * @param currentUserEmail The email of the club owner
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
}
