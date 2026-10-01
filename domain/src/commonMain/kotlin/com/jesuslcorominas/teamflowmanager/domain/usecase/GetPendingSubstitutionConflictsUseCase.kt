package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair

/**
 * Pairs already scheduled for [matchId] that share a player with [pair], and that scheduling it
 * would therefore discard. Empty when [pair] displaces nothing.
 *
 * Separate from [AddPendingSubstitutionUseCase] on purpose: the answer is needed *before*
 * deciding whether to write, because a confirmation from the coach sits in between. Merging the
 * two would push that policy down into the use case layer, where it does not belong.
 */
interface GetPendingSubstitutionConflictsUseCase {
    operator fun invoke(
        matchId: String,
        pair: SubstitutionPair,
    ): List<SubstitutionPair>
}
