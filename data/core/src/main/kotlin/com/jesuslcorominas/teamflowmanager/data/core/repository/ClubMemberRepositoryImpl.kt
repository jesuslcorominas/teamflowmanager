package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository

internal class ClubMemberRepositoryImpl(
    private val clubMemberDataSource: ClubMemberDataSource
) : ClubMemberRepository {
    override suspend fun createOrUpdateClubMember(clubMember: ClubMember) {
        clubMemberDataSource.createOrUpdateClubMember(clubMember)
    }

    override suspend fun getClubMember(userId: String, clubFirestoreId: String): ClubMember? =
        clubMemberDataSource.getClubMember(userId, clubFirestoreId)
}
