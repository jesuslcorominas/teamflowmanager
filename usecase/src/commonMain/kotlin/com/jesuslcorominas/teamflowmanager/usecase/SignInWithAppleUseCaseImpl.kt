package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithAppleUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository

internal class SignInWithAppleUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignInWithAppleUseCase {
    override suspend fun invoke(credential: AppleSignInCredential): Result<User> {
        val result = authRepository.signInWithApple(credential)
        result.getOrNull()?.let { user ->
            authRepository.saveUserToFirestore(user)
        }
        return result
    }
}
