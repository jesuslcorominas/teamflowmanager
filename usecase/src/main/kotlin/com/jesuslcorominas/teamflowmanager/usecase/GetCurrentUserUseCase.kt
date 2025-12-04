package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

interface GetCurrentUserUseCase {
    operator fun invoke(): Flow<User?>
}

internal class GetCurrentUserUseCaseImpl(
    private val authRepository: AuthRepository
) : GetCurrentUserUseCase {
    override fun invoke(): Flow<User?> = authRepository.getCurrentUser()
}
