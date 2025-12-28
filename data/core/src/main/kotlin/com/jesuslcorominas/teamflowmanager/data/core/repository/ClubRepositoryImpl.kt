package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

internal class ClubRepositoryImpl(
    private val clubDataSource: ClubDataSource
) : ClubRepository {
    override fun findClubByInvitationCode(invitationCode: String): Flow<Club?> =
        clubDataSource.findClubByInvitationCode(invitationCode)

    override fun getClubByFirestoreId(firestoreId: String): Flow<Club?> =
        clubDataSource.getClubByFirestoreId(firestoreId)
}
