package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match

interface UpdateMatchUseCase {
    suspend operator fun invoke(match: Match)
}
