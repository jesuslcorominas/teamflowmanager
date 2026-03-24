package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithGoogleUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository

internal class SignInWithGoogleUseCaseImpl(
    private val authRepository: AuthRepository
) : SignInWithGoogleUseCase {
    override suspend fun invoke(idToken: String): Result<User> {
        val result = authRepository.signInWithGoogle(idToken)
        result.getOrNull()?.let { user ->
            authRepository.saveUserToFirestore(user)
        }
        return result
    }
}
