package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface GetTeamsByClubUseCase {
    /**
     * Get all teams for a club.
     * @param clubId The identifier of the club
     * @return Flow emitting list of teams in the club
     */
    operator fun invoke(clubId: String): Flow<List<Team>>
}
