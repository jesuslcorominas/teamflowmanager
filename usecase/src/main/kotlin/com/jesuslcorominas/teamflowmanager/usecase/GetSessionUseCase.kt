package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Session
import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

interface GetSessionUseCase {
    operator fun invoke(): Flow<Session?>
}

internal class GetSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : GetSessionUseCase {
    override fun invoke(): Flow<Session?> = sessionRepository.getSession()
}
