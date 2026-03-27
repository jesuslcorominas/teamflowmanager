package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository

internal class ClubRepositoryImpl(
    private val clubDataSource: ClubDataSource,
) : ClubRepository {
    override suspend fun createClubWithOwner(
        clubName: String,
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String,
    ): Club {
        return clubDataSource.createClubWithOwner(clubName, currentUserId, currentUserName, currentUserEmail)
    }

    override suspend fun getClubByInvitationCode(invitationCode: String): Club? {
        return clubDataSource.getClubByInvitationCode(invitationCode)
    }

    override suspend fun getClubByFirestoreId(firestoreId: String): Club? {
        return clubDataSource.getClubByFirestoreId(firestoreId)
    }

    override suspend fun regenerateInvitationCode(firestoreId: String): String = clubDataSource.regenerateInvitationCode(firestoreId)

    override suspend fun updateClub(
        firestoreId: String,
        name: String,
        homeGround: String?,
    ): Club {
        return clubDataSource.updateClub(firestoreId, name, homeGround)
    }
}
