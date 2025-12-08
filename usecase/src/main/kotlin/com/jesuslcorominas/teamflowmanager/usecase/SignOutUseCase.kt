package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository

interface SignOutUseCase {
    suspend operator fun invoke()
}

internal class SignOutUseCaseImpl(
    private val authRepository: AuthRepository
) : SignOutUseCase {
    override suspend fun invoke() = authRepository.signOut()
}
