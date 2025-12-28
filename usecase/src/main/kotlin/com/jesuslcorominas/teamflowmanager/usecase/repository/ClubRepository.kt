package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import kotlinx.coroutines.flow.Flow

interface ClubRepository {
    /**
     * Find a club by its invitation code.
     * @param invitationCode The invitation code to search for
     * @return Flow emitting the club if found, null otherwise
     */
    fun findClubByInvitationCode(invitationCode: String): Flow<Club?>

    /**
     * Get a club by its Firestore document ID.
     * @param firestoreId The Firestore document ID
     * @return Flow emitting the club if found, null otherwise
     */
    fun getClubByFirestoreId(firestoreId: String): Flow<Club?>
}
