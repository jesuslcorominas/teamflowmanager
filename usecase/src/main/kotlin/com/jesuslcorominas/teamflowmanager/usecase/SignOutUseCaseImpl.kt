package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository


internal class SignOutUseCaseImpl(
    private val authRepository: AuthRepository
) : SignOutUseCase {
    override suspend fun invoke() = authRepository.signOut()
}
