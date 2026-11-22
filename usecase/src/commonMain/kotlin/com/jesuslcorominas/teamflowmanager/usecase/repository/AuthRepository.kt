package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>

    suspend fun signInWithGoogle(idToken: String): Result<User>

    suspend fun signInWithApple(credential: AppleSignInCredential): Result<User>

    suspend fun signOut()

    suspend fun saveUserToFirestore(user: User)

    /**
     * Revokes the third-party tokens that need explicit revocation when the
     * account is deleted (Sign in with Apple). A no-op for providers that do not.
     */
    suspend fun revokeSignInProviderTokens(): Result<Unit>
}
