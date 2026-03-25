package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
) : AuthRepository {
    override fun getCurrentUser(): Flow<User?> = authDataSource.getCurrentUser()

    override suspend fun signInWithGoogle(idToken: String): Result<User> = authDataSource.signInWithGoogle(idToken)

    override suspend fun signOut() = authDataSource.signOut()

    override suspend fun saveUserToFirestore(user: User) = authDataSource.saveUserToFirestore(user)
}
