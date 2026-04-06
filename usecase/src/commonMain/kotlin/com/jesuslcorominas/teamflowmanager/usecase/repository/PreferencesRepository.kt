package com.jesuslcorominas.teamflowmanager.usecase.repository

interface PreferencesRepository {
    fun shouldShowInvalidSubstitutionAlert(): Boolean

    fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean)

    fun getDefaultCaptainId(): Long?

    fun setDefaultCaptainId(playerId: Long?)

    fun hasNotificationPermissionBeenRequested(): Boolean

    fun setNotificationPermissionRequested(requested: Boolean)

    fun getActiveViewRole(): String?

    fun setActiveViewRole(role: String)
}
