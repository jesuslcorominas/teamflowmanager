package com.jesuslcorominas.teamflowmanager.domain.utils

/**
 * Revokes the Sign in with Apple tokens of the signed-in user, as App Store
 * guideline 5.1.1(v) requires when an account is deleted.
 *
 * Implemented natively on iOS, where it asks Apple for a fresh authorization
 * code and hands it to Firebase. There is no Android implementation: Android
 * does not offer Sign in with Apple.
 */
interface AppleCredentialRevoker {
    suspend fun revoke(): Result<Unit>
}
