package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

// 2 minutes: long enough to tolerate a poor network on a football pitch without killing a live
// operation on another device of the same team, but short enough to recover quickly when the app
// dies mid-operation. A healthy operation completes in seconds; nothing legitimate should still
// be IN_PROGRESS after two minutes.
private const val STALE_OPERATION_THRESHOLD_MILLIS = 2 * 60 * 1000L

internal class GetMatchByIdUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val matchOperationRepository: MatchOperationRepository,
) : GetMatchByIdUseCase {
    /**
     * Returns a [Flow] of the match with the given [matchId].
     *
     * Before emitting the first value, sweeps any IN_PROGRESS operations that are older than
     * [STALE_OPERATION_THRESHOLD_MILLIS] and marks them FAILED. The sweep runs once per
     * collection start (via [onStart]), not on every downstream emission. Re-subscribing does
     * run it again, so the query is repeated on every reconnect; the age threshold is what
     * keeps a live operation from another device from ever being swept.
     */
    override fun invoke(matchId: String): Flow<Match?> =
        matchRepository.getMatchById(matchId)
            .onStart {
                matchOperationRepository.markStaleOperationsAsFailed(
                    matchId,
                    STALE_OPERATION_THRESHOLD_MILLIS,
                )
            }
}
