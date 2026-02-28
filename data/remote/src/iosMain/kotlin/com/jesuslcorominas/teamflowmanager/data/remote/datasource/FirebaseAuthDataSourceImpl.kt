package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.User
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthDataSourceImpl(private val firebaseAuth: FirebaseAuth) : AuthDataSource {

    override fun getCurrentUser(): Flow<User?> =
        firebaseAuth.authStateChanged.map { firebaseUser ->
            firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoURL,
                )
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
            val authResult = firebaseAuth.signInWithCredential(credential)
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Firebase user is null after sign in"))
            Result.success(
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoURL,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun saveUserToFirestore(user: User) {
        // No-op for iOS — user data is managed server-side via Firebase Auth
    }
}
