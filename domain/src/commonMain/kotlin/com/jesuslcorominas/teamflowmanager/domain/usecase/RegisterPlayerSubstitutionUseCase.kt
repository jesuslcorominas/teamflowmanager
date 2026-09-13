package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionBatchResult
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair

interface RegisterPlayerSubstitutionUseCase {
    /**
     * Applies every valid pair of [substitutions] under a single operation, and reports which ones
     * were applied and which were discarded. An invalid pair never aborts the rest of the batch.
     */
    suspend operator fun invoke(
        matchId: String,
        substitutions: List<SubstitutionPair>,
        currentTimeMillis: Long,
    ): SubstitutionBatchResult
}
