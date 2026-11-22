package com.jesuslcorominas.teamflowmanager.data.core.datasource

interface PreferencesDataSource {
    fun shouldShowInvalidSubstitutionAlert(): Boolean

    fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean)

    fun getDefaultCaptainId(): String?

    fun setDefaultCaptainId(playerId: String?)

    fun hasNotificationPermissionBeenRequested(): Boolean

    fun setNotificationPermissionRequested(requested: Boolean)

    fun getActiveViewRole(): String?

    fun setActiveViewRole(role: String)

    fun getSubstitutionMode(): String?

    fun setSubstitutionMode(mode: String)

    /**
     * Removes the values tied to the signed-in account (active view role, default captain).
     * Device-wide settings stay, so the next account starts clean without losing them.
     */
    fun clearAccountData()
}
