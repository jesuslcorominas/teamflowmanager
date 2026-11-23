package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.AccountExistsWithDifferentProviderException
import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.utils.AppleCredentialRevoker
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import platform.Foundation.NSLog
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthDataSourceImpl(
    private val firebaseAuth: FirebaseAuth,
    /** Native Sign in with Apple revocation; null when the app did not register one. */
    private val appleCredentialRevoker: AppleCredentialRevoker? = null,
) : AuthDataSource {
    private companion object {
        const val APPLE_PROVIDER_ID = "apple.com"
    }

    override fun getCurrentUser(): Flow<User?> =
        firebaseAuth.authStateChanged.map { firebaseUser ->
            firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email,
                    displayName = resolveDisplayName(it.displayName, it.email),
                    photoUrl = it.photoURL,
                )
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // idToken is encoded as "idToken\naccessToken" by GoogleSignInBridge.
            // Both tokens are required by the native Firebase iOS SDK.
            val parts = idToken.split("\n")
            val actualIdToken = parts[0]
            val accessToken = parts.getOrElse(1) { "" }
            val credential = GoogleAuthProvider.credential(idToken = actualIdToken, accessToken = accessToken)
            val authResult = firebaseAuth.signInWithCredential(credential)
            val firebaseUser =
                authResult.user
                    ?: return Result.failure(Exception("Firebase user is null after sign in"))
            Result.success(
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = resolveDisplayName(firebaseUser.displayName, firebaseUser.email),
                    photoUrl = firebaseUser.photoURL,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithApple(credential: AppleSignInCredential): Result<User> {
        return try {
            val oauthCredential =
                OAuthProvider.credential(
                    providerId = APPLE_PROVIDER_ID,
                    idToken = credential.idToken,
                    rawNonce = credential.rawNonce,
                )
            val authResult = firebaseAuth.signInWithCredential(oauthCredential)
            val firebaseUser =
                authResult.user
                    ?: return Result.failure(Exception("Firebase user is null after sign in"))
            storeNameOnFirstSignIn(firebaseUser, credential.fullName)
            // Read back from currentUser: updateProfile does not refresh the
            // instance returned by signInWithCredential on every SDK version.
            val signedIn = firebaseAuth.currentUser ?: firebaseUser
            Result.success(
                User(
                    id = signedIn.uid,
                    email = signedIn.email,
                    displayName =
                        resolveDisplayName(
                            signedIn.displayName ?: credential.fullName?.takeIf { it.isNotBlank() },
                            signedIn.email,
                        ),
                    photoUrl = signedIn.photoURL,
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthUserCollisionException) {
            // Same email already registered with Google. Accounts are not linked
            // automatically; the UI asks the user to sign in with Google instead.
            Result.failure(AccountExistsWithDifferentProviderException(e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creating or joining a club and accepting an invitation require a display name.
     * Apple sends the name only on the first authorisation, and the user may not share
     * it at all, so an Apple account can have none: fall back to the local part of the
     * email (the relay address when the user hides it). Not stored on the profile, so
     * a real name is never overwritten.
     */
    private fun resolveDisplayName(
        displayName: String?,
        email: String?,
    ): String? =
        displayName?.trim()?.takeIf { it.isNotEmpty() }
            ?: email?.substringBefore('@')?.trim()?.takeIf { it.isNotEmpty() }

    /**
     * Apple sends the user's name only the first time they authorise the app, so
     * it is stored on the Firebase profile then. Best effort: failing to store it
     * must not fail the sign-in.
     */
    private suspend fun storeNameOnFirstSignIn(
        firebaseUser: FirebaseUser,
        fullName: String?,
    ) {
        val name = fullName?.trim()?.takeIf { it.isNotEmpty() } ?: return
        if (!firebaseUser.displayName.isNullOrBlank()) return
        try {
            firebaseUser.updateProfile(displayName = name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NSLog("TeamFlowManager: could not store the Apple display name: %@", e.message ?: e.toString())
        }
    }

    override suspend fun revokeSignInProviderTokens(): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.success(Unit)
        if (user.providerData.none { it.providerId == APPLE_PROVIDER_ID }) return Result.success(Unit)

        val revoker = appleCredentialRevoker
        if (revoker == null) {
            NSLog("TeamFlowManager: Sign in with Apple revocation skipped, no revoker registered")
            return Result.failure(IllegalStateException("No Apple credential revoker registered"))
        }
        return try {
            revoker.revoke().onFailure { e ->
                // Expected until the Sign in with Apple key is configured on the
                // Firebase Apple provider. Logged, never shown: the deletion goes on.
                NSLog("TeamFlowManager: Sign in with Apple token revocation failed: %@", e.message ?: e.toString())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NSLog("TeamFlowManager: Sign in with Apple token revocation failed: %@", e.message ?: e.toString())
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun getIdToken(): String? {
        return try {
            // Forced refresh: a cached token can be minutes old, and the Cloud
            // Function rejects anything it cannot verify.
            firebaseAuth.currentUser?.getIdToken(forceRefresh = true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveUserToFirestore(user: User) {
        // No-op for iOS — user data is managed server-side via Firebase Auth
    }
}
