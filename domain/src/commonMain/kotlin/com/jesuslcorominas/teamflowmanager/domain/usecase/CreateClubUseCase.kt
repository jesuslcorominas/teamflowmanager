package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club

interface CreateClubUseCase {
    /**
     * Create a new club with the current user as owner and president.
     * @param clubName The name of the club to create
     * @return The created club with generated invitation code
     */
    suspend operator fun invoke(clubName: String): Club
}
