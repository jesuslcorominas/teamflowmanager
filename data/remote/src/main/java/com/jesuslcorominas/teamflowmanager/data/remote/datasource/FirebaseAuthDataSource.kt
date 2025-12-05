package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthDataSource(
    private val firebaseAuth: FirebaseAuth
) : AuthDataSource {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl?.toString()
                )
            }
            trySend(user)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Firebase user is null after sign in"))

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString()
            )
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun saveUserToFirestore(user: User) {
        // Note: This is a placeholder for Firestore integration
        // When Firestore is added, this will save user data to the 'users' collection
        // For now, user data is only stored in Firebase Auth
    }
}
