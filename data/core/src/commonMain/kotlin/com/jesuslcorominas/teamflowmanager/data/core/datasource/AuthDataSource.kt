package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    fun getCurrentUser(): Flow<User?>

    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Sign in with Apple. Only the iOS implementation supports it; the default
     * keeps platforms without the provider (Android) untouched.
     */
    suspend fun signInWithApple(credential: AppleSignInCredential): Result<User> =
        Result.failure(UnsupportedOperationException("Sign in with Apple is not available on this platform"))

    /**
     * Revokes the provider tokens that need explicit revocation when the account
     * is deleted (Sign in with Apple). No-op by default.
     */
    suspend fun revokeSignInProviderTokens(): Result<Unit> = Result.success(Unit)

    suspend fun signOut()

    suspend fun saveUserToFirestore(user: User)

    /**
     * A freshly minted Firebase ID token for the signed-in user, or null when
     * nobody is signed in.
     *
     * Needed to authenticate calls to our own Cloud Functions, which verify the
     * token rather than trusting a uid sent in the body.
     */
    suspend fun getIdToken(): String?
}
