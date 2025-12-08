package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    fun getCurrentUser(): Flow<User?>

    suspend fun signInWithGoogle(idToken: String): Result<User>

    suspend fun signOut()

    suspend fun saveUserToFirestore(user: User)
}
