package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchTimeline
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get all timeline events for a finished match.
 */
interface GetMatchTimelineUseCase {
    operator fun invoke(matchId: Long, teamId: String? = null): Flow<MatchTimeline?>
}
