package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.coroutines.flow.Flow

interface GetUserClubMembershipUseCase {
    /**
     * Get the club membership for the current authenticated user.
     * Returns null if the user is not a member of any club.
     */
    operator fun invoke(): Flow<ClubMember?>
}
