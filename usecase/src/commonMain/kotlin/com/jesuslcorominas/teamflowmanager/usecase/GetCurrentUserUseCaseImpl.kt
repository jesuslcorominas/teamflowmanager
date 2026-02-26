package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import kotlinx.coroutines.flow.Flow



internal class GetCurrentUserUseCaseImpl(
    private val authRepository: AuthRepository
) : GetCurrentUserUseCase {
    override fun invoke(): Flow<User?> = authRepository.getCurrentUser()
}
