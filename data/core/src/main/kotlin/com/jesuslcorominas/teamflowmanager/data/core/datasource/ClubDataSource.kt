package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember

interface ClubDataSource {
    /**
     * Create a new club with the current user as owner and president.
     * This operation should be atomic - both club and clubMember must be created together.
     * @param clubName The name of the club to create
     * @param currentUserId The user ID of the club owner
     * @param currentUserName The display name of the club owner
     * @param currentUserEmail The email of the club owner
     * @return The created club with generated invitation code
     */
    suspend fun createClubWithOwner(clubName: String, currentUserId: String, currentUserName: String, currentUserEmail: String): Club
}
