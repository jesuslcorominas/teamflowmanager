package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import kotlinx.coroutines.flow.Flow

internal class GetClubMembersUseCaseImpl(
    private val clubMemberRepository: ClubMemberRepository,
) : GetClubMembersUseCase {
    override fun invoke(clubFirestoreId: String): Flow<List<ClubMember>> {
        require(clubFirestoreId.isNotBlank()) {
            "Club Firestore ID cannot be blank"
        }
        return clubMemberRepository.getClubMembers(clubFirestoreId)
    }
}
