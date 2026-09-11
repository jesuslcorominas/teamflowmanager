package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair

interface RegisterPlayerSubstitutionUseCase {
    suspend operator fun invoke(
        matchId: String,
        substitutions: List<SubstitutionPair>,
        currentTimeMillis: Long,
    )
}
