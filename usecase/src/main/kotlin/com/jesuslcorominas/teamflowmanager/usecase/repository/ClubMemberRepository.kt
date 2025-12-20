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
}
