package com.jesuslcorominas.teamflowmanager.data.core.datasource

interface PreferencesLocalDataSource {
    fun shouldShowInvalidSubstitutionAlert(): Boolean
    
    fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean)
    
    fun getDefaultCaptainId(): Long?
    
    fun setDefaultCaptainId(playerId: Long?)
    
    fun hasNotificationPermissionBeenRequested(): Boolean
    
    fun setNotificationPermissionRequested(requested: Boolean)
}
