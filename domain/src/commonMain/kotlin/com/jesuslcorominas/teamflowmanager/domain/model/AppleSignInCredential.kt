package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * What the native Sign in with Apple flow hands over to the auth layer.
 *
 * [rawNonce] is the unhashed nonce whose SHA-256 was sent to Apple; Firebase
 * needs it to check the [idToken]. [fullName] is only present the first time a
 * user authorises the app — Apple never sends it again.
 */
data class AppleSignInCredential(
    val idToken: String,
    val rawNonce: String,
    val fullName: String? = null,
)
