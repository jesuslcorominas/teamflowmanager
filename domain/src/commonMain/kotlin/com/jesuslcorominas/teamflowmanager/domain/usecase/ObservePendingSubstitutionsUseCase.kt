package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.coroutines.flow.Flow

/**
 * Emits the substitutions scheduled for [matchId] and every later change, in insertion order.
 * A match with nothing scheduled emits an empty list.
 *
 * This use case — like its four siblings over the same store — is deliberately a thin delegation.
 * It exists so the match screen can reach the pending store through `domain` instead of depending
 * on `usecase.repository`, which would break the layering the architecture relies on.
 */
interface ObservePendingSubstitutionsUseCase {
    operator fun invoke(matchId: String): Flow<List<SubstitutionPair>>
}
