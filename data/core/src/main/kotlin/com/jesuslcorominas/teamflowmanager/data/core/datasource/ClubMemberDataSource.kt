package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember

interface ClubMemberDataSource {
    /**
     * Create or update a club member.
     * If a member with the same userId and clubId already exists, it will be updated.
     * @param clubMember The club member to create or update
     */
    suspend fun createOrUpdateClubMember(clubMember: ClubMember)

    /**
     * Get a club member by userId and club Firestore ID.
     * @param userId The Firebase user ID
     * @param clubFirestoreId The club's Firestore document ID
     * @return The club member if found, null otherwise
     */
    suspend fun getClubMember(userId: String, clubFirestoreId: String): ClubMember?
}
