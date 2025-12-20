package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import kotlinx.coroutines.flow.Flow

internal class ClubMemberRepositoryImpl(
    private val clubMemberDataSource: ClubMemberDataSource
) : ClubMemberRepository {
    override fun getClubMemberByUserId(userId: String): Flow<ClubMember?> =
        clubMemberDataSource.getClubMemberByUserId(userId)
}
