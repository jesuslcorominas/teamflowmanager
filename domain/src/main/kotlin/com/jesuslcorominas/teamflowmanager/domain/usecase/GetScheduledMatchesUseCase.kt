package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match

interface GetScheduledMatchesUseCase {
    suspend operator fun invoke(): List<Match>
}
