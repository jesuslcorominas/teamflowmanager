package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import kotlinx.coroutines.flow.Flow

internal class GetClubMembersUseCaseImpl(
    private val clubMemberRepository: ClubMemberRepository,
) : GetClubMembersUseCase {
    override fun invoke(clubId: String): Flow<List<ClubMember>> {
        require(clubId.isNotBlank()) {
            "Club ID cannot be blank"
        }
        return clubMemberRepository.getClubMembers(clubId)
    }
}
