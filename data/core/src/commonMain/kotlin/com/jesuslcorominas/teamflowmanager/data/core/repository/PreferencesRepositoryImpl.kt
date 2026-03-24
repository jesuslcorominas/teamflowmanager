package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class PreferencesRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
) : PreferencesRepository {

    override fun shouldShowInvalidSubstitutionAlert(): Boolean {
        return preferencesDataSource.shouldShowInvalidSubstitutionAlert()
    }

    override fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean) {
        preferencesDataSource.setShouldShowInvalidSubstitutionAlert(shouldShow)
    }

    override fun getDefaultCaptainId(): Long? {
        return preferencesDataSource.getDefaultCaptainId()
    }

    override fun setDefaultCaptainId(playerId: Long?) {
        preferencesDataSource.setDefaultCaptainId(playerId)
    }

    override fun hasNotificationPermissionBeenRequested(): Boolean {
        return preferencesDataSource.hasNotificationPermissionBeenRequested()
    }

    override fun setNotificationPermissionRequested(requested: Boolean) {
        preferencesDataSource.setNotificationPermissionRequested(requested)
    }
}
