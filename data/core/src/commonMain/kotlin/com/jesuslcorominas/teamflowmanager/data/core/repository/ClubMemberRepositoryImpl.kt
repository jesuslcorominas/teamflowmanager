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

    override fun getClubMembers(clubFirestoreId: String): Flow<List<ClubMember>> =
        clubMemberDataSource.getClubMembers(clubFirestoreId)

    override suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubId: Long,
        clubFirestoreId: String,
        roles: List<String>
    ): ClubMember {
        return clubMemberDataSource.createOrUpdateClubMember(
            userId, name, email, clubId, clubFirestoreId, roles
        )
    }

    override suspend fun updateClubMemberRoles(
        userId: String,
        clubFirestoreId: String,
        roles: List<String>
    ) {
        clubMemberDataSource.updateClubMemberRoles(userId, clubFirestoreId, roles)
    }

    override suspend fun addClubMemberRole(
        userId: String,
        clubFirestoreId: String,
        role: String
    ) {
        clubMemberDataSource.addClubMemberRole(userId, clubFirestoreId, role)
    }

    override suspend fun getClubMemberByUserIdAndClub(
        userId: String,
        clubFirestoreId: String
    ): ClubMember? {
        return clubMemberDataSource.getClubMemberByUserIdAndClub(userId, clubFirestoreId)
    }
}
