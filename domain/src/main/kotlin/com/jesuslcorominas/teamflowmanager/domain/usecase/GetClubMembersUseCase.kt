package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting all members of a club.
 */
interface GetClubMembersUseCase {
    /**
     * Get all members of a club.
     * 
     * @param clubFirestoreId The Firestore document ID of the club
     * @return Flow emitting list of club members
     */
    operator fun invoke(clubFirestoreId: String): Flow<List<ClubMember>>
}
