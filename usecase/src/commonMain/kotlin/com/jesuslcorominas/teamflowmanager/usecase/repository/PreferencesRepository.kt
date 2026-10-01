package com.jesuslcorominas.teamflowmanager.usecase.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun shouldShowInvalidSubstitutionAlert(): Boolean

    fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean)

    fun getDefaultCaptainId(): String?

    fun setDefaultCaptainId(playerId: String?)

    fun hasNotificationPermissionBeenRequested(): Boolean

    fun setNotificationPermissionRequested(requested: Boolean)

    fun getActiveViewRole(): String?

    fun setActiveViewRole(role: String)

    /** Emits the stored active view role and every later change made through [setActiveViewRole]. */
    fun observeActiveViewRole(): Flow<String?>

    fun setSubstitutionMode(mode: String)

    /** Emits the stored substitution mode and every later change made through [setSubstitutionMode]. */
    fun observeSubstitutionMode(): Flow<String?>
}
