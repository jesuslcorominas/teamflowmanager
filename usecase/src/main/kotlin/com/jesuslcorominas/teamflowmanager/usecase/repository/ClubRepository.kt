package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember

interface ClubRepository {
    /**
     * Create a new club with the current user as owner and president.
     * @param clubName The name of the club to create
     * @return The created club with generated invitation code
     */
    suspend fun createClubWithOwner(clubName: String, currentUserId: String, currentUserName: String, currentUserEmail: String): Club
}
