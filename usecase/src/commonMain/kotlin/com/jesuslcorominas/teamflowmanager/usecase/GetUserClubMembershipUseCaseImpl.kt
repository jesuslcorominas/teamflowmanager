package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetUserClubMembershipUseCaseImpl(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val clubMemberRepository: ClubMemberRepository
) : GetUserClubMembershipUseCase {
    override fun invoke(): Flow<ClubMember?> {
        return getCurrentUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                clubMemberRepository.getClubMemberByUserId(user.id)
            }
        }
    }
}
