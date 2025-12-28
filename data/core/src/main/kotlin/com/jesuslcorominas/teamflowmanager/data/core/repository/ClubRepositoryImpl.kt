package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository

internal class ClubRepositoryImpl(
    private val clubDataSource: ClubDataSource
) : ClubRepository {
    override suspend fun createClubWithOwner(
        clubName: String,
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String
    ): Club {
        return clubDataSource.createClubWithOwner(clubName, currentUserId, currentUserName, currentUserEmail)
    }
}
