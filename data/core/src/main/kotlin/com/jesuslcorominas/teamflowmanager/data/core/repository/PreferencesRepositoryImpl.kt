package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesLocalDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class PreferencesRepositoryImpl(
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
) : PreferencesRepository {
    
    override fun shouldShowInvalidSubstitutionAlert(): Boolean {
        return preferencesLocalDataSource.shouldShowInvalidSubstitutionAlert()
    }
    
    override fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean) {
        preferencesLocalDataSource.setShouldShowInvalidSubstitutionAlert(shouldShow)
    }
    
    override fun getDefaultCaptainId(): Long? {
        return preferencesLocalDataSource.getDefaultCaptainId()
    }
    
    override fun setDefaultCaptainId(playerId: Long?) {
        preferencesLocalDataSource.setDefaultCaptainId(playerId)
    }
}
