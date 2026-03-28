package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import kotlinx.coroutines.flow.Flow

internal class ClubMemberRepositoryImpl(
    private val clubMemberDataSource: ClubMemberDataSource,
) : ClubMemberRepository {
    override fun getClubMemberByUserId(userId: String): Flow<ClubMember?> = clubMemberDataSource.getClubMemberByUserId(userId)

    override fun getClubMembers(clubId: String): Flow<List<ClubMember>> = clubMemberDataSource.getClubMembers(clubId)

    override suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubNumericId: Long,
        clubId: String,
        roles: List<String>,
    ): ClubMember {
        return clubMemberDataSource.createOrUpdateClubMember(
            userId,
            name,
            email,
            clubNumericId,
            clubId,
            roles,
        )
    }

    override suspend fun updateClubMemberRoles(
        userId: String,
        clubId: String,
        roles: List<String>,
    ) {
        clubMemberDataSource.updateClubMemberRoles(userId, clubId, roles)
    }

    override suspend fun addClubMemberRole(
        userId: String,
        clubId: String,
        role: String,
    ) {
        clubMemberDataSource.addClubMemberRole(userId, clubId, role)
    }

    override suspend fun getClubMemberByUserIdAndClub(
        userId: String,
        clubId: String,
    ): ClubMember? {
        return clubMemberDataSource.getClubMemberByUserIdAndClub(userId, clubId)
    }
}
