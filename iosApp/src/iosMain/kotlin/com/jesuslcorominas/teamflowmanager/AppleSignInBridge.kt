package com.jesuslcorominas.teamflowmanager

import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.utils.AppleCredentialRevoker
import kotlinx.coroutines.CompletableDeferred

/**
 * Bridge between Kotlin and the native Sign in with Apple flow (Swift's
 * AppleSignInCallbackImpl, built on AuthenticationServices). Same shape as
 * [GoogleSignInBridge]: Kotlin suspends on a deferred, Swift completes it.
 *
 * Two flows go through it:
 *  - [signIn]: the login button. Swift generates the nonce, shows Apple's sheet
 *    and returns the identity token, the raw nonce and, the first time only, the
 *    user's name. Returns null when the user dismisses the sheet.
 *  - [revoke]: account deletion. Swift asks Apple for a fresh authorization code
 *    and hands it to Firebase (`Auth.auth().revokeToken(withAuthorizationCode:)`).
 *
 * Swift setup (in iOSApp.swift):
 * ```swift
 * AppleSignInBridge.shared.callback = AppleSignInCallbackImpl()
 * ```
 */
object AppleSignInBridge : AppleCredentialRevoker {
    /** Set from Swift at launch. */
    var callback: AppleSignInCallback? = null

    private var signInDeferred: CompletableDeferred<AppleSignInCredential?>? = null
    private var revokeDeferred: CompletableDeferred<Result<Unit>>? = null

    suspend fun signIn(): AppleSignInCredential? {
        val d = CompletableDeferred<AppleSignInCredential?>()
        // A second tap while Apple's sheet is up replaces the pending request;
        // the old one is resolved as dismissed so nobody waits forever.
        signInDeferred?.complete(null)
        signInDeferred = d
        val cb = callback
        if (cb == null) {
            d.completeExceptionally(IllegalStateException("Sign in with Apple handler not configured"))
        } else {
            cb.onSignInRequested()
        }
        return d.await()
    }

    /** Called from Swift after Apple returned a credential. */
    fun onSignInSuccess(
        idToken: String,
        rawNonce: String,
        fullName: String?,
    ) {
        signInDeferred?.complete(
            AppleSignInCredential(
                idToken = idToken,
                rawNonce = rawNonce,
                fullName = fullName?.takeIf { it.isNotBlank() },
            ),
        )
        signInDeferred = null
    }

    /** Called from Swift when the user dismisses Apple's sheet. */
    fun onSignInCancelled() {
        signInDeferred?.complete(null)
        signInDeferred = null
    }

    /** Called from Swift when the sign-in fails. */
    fun onSignInError(message: String) {
        signInDeferred?.completeExceptionally(Exception(message))
        signInDeferred = null
    }

    override suspend fun revoke(): Result<Unit> {
        val cb = callback ?: return Result.failure(IllegalStateException("Sign in with Apple handler not configured"))
        val d = CompletableDeferred<Result<Unit>>()
        revokeDeferred?.complete(Result.failure(IllegalStateException("Superseded by a new revocation")))
        revokeDeferred = d
        cb.onRevokeRequested()
        return d.await()
    }

    /** Called from Swift once Firebase revoked the Apple tokens. */
    fun onRevokeSuccess() {
        revokeDeferred?.complete(Result.success(Unit))
        revokeDeferred = null
    }

    /** Called from Swift when getting the code or revoking fails (also on cancel). */
    fun onRevokeError(message: String) {
        revokeDeferred?.complete(Result.failure(Exception(message)))
        revokeDeferred = null
    }
}

/**
 * Implemented in Swift (AppleSignInCallbackImpl). Each request is answered
 * through the matching AppleSignInBridge.shared.on… callback.
 */
interface AppleSignInCallback {
    fun onSignInRequested()

    fun onRevokeRequested()
}
