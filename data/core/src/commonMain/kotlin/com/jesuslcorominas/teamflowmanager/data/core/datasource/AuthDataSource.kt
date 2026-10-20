package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    fun getCurrentUser(): Flow<User?>

    suspend fun signInWithGoogle(idToken: String): Result<User>

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
