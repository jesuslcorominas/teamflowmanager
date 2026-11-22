package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class SignOutUseCaseImpl(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
) : SignOutUseCase {
    override suspend fun invoke() {
        authRepository.signOut()
        // After the auth sign-out, so a failed sign-out leaves the current account untouched.
        preferencesRepository.clearAccountData()
    }
}
