package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
) : AuthRepository {
    override fun getCurrentUser(): Flow<User?> = authDataSource.getCurrentUser()

    override suspend fun signInWithGoogle(idToken: String): Result<User> = authDataSource.signInWithGoogle(idToken)

    override suspend fun signInWithApple(credential: AppleSignInCredential): Result<User> = authDataSource.signInWithApple(credential)

    override suspend fun revokeSignInProviderTokens(): Result<Unit> = authDataSource.revokeSignInProviderTokens()

    override suspend fun signOut() = authDataSource.signOut()

    override suspend fun saveUserToFirestore(user: User) = authDataSource.saveUserToFirestore(user)
}
