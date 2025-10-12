package com.jesuslcorominas.teamflowmanager.usecase.repository

interface PreferencesRepository {
    fun shouldShowInvalidSubstitutionAlert(): Boolean
    
    fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean)
}
