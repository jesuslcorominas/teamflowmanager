package com.jesuslcorominas.teamflowmanager.data.core.datasource

/**
 * Triggers the server-side account deletion cascade.
 *
 * Implementations call the `deleteAccount` Cloud Function with the signed-in
 * user's ID token; the cascade runs with Admin privileges there rather than in
 * the client. See docs/ACCOUNT_DELETION.md.
 */
interface AccountDeletionDataSource {
    suspend fun deleteAccount(): Result<Unit>
}
