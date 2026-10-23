package com.jesuslcorominas.teamflowmanager.usecase.repository

interface AccountRepository {
    /**
     * Deletes the signed-in user's account server-side, cascading through their
     * clubs, teams, players, matches, stored photos and authentication record.
     *
     * Returns a failure if any part of the cascade did not complete, so the
     * caller never reports a deletion that only half happened.
     */
    suspend fun deleteAccount(): Result<Unit>
}
