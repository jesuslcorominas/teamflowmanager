package com.jesuslcorominas.teamflowmanager

import kotlinx.coroutines.CompletableDeferred

/**
 * KMP-17: Bridge between Compose (Kotlin) and the native Google Sign-In iOS SDK (Swift).
 *
 * Flow:
 *  1. Kotlin coroutine calls [signIn] (suspend) — stores a CompletableDeferred and
 *     notifies Swift via [callback.onSignInRequested].
 *  2. Swift's GoogleSignInCallbackImpl runs GIDSignIn.signIn(withPresenting:).
 *  3. On completion, Swift calls [onSuccess] or [onError] to resume the coroutine.
 *
 * Swift setup (in iOSApp.swift):
 * ```swift
 * GoogleSignInBridge.shared.callback = GoogleSignInCallbackImpl()
 * ```
 */
object GoogleSignInBridge {

    /**
     * Set from Swift before the login screen is shown.
     * Swift class must implement onSignInRequested() by starting GIDSignIn.
     */
    var callback: GoogleSignInCallback? = null

    private var deferred: CompletableDeferred<String>? = null

    /**
     * Called by Kotlin (Compose button press) to trigger the native sign-in flow.
     * Suspends until Swift calls [onSuccess] or [onError].
     */
    suspend fun signIn(): String {
        val d = CompletableDeferred<String>()
        deferred = d
        val cb = callback
        if (cb == null) {
            d.completeExceptionally(IllegalStateException("Google Sign-In handler not configured"))
        } else {
            cb.onSignInRequested()
        }
        return d.await()
    }

    /** Called from Swift after a successful Google Sign-In with the user's ID token. */
    fun onSuccess(idToken: String) {
        deferred?.complete(idToken)
        deferred = null
    }

    /** Called from Swift when the sign-in fails or is cancelled. */
    fun onError(message: String) {
        deferred?.completeExceptionally(Exception(message))
        deferred = null
    }
}

/**
 * Single-method interface implemented in Swift.
 * Triggers the native Google Sign-In UI (GIDSignIn.sharedInstance.signIn).
 * After sign-in, call GoogleSignInBridge.shared.onSuccess/onError.
 */
interface GoogleSignInCallback {
    fun onSignInRequested()
}
