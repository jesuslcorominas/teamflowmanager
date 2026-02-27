package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.User
import dev.gitlive.firebase.auth.FirebaseAuth
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
        // Google Sign-In on iOS requires the native GoogleSignIn iOS SDK (KMP-17).
        // For the Phase 2 MVP, authentication is done via email/password.
        throw NotImplementedError("Google Sign-In for iOS will be implemented in KMP-17")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun saveUserToFirestore(user: User) {
        // No-op for iOS Phase 2 MVP — Firestore user saving is handled on Android
    }
}
