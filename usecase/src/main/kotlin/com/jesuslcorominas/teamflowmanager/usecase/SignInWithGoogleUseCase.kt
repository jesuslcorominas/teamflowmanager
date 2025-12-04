package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository

interface SignInWithGoogleUseCase {
    suspend operator fun invoke(idToken: String): Result<User>
}

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
