package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PreferencesRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
) : PreferencesRepository {
    /**
     * Key-value storage (SharedPreferences / NSUserDefaults) has no change notification, so the
     * repository keeps the current role in a flow and updates it on write. Seeded from storage so
     * the first emission is the persisted value, not a default.
     */
    private val activeViewRole = MutableStateFlow(preferencesDataSource.getActiveViewRole())

    /** Same reasoning as [activeViewRole]: seeded from storage, updated on every write. */
    private val substitutionMode = MutableStateFlow(preferencesDataSource.getSubstitutionMode())

    override fun shouldShowInvalidSubstitutionAlert(): Boolean {
        return preferencesDataSource.shouldShowInvalidSubstitutionAlert()
    }

    override fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean) {
        preferencesDataSource.setShouldShowInvalidSubstitutionAlert(shouldShow)
    }

    override fun getDefaultCaptainId(): String? {
        return preferencesDataSource.getDefaultCaptainId()
    }

    override fun setDefaultCaptainId(playerId: String?) {
        preferencesDataSource.setDefaultCaptainId(playerId)
    }

    override fun hasNotificationPermissionBeenRequested(): Boolean {
        return preferencesDataSource.hasNotificationPermissionBeenRequested()
    }

    override fun setNotificationPermissionRequested(requested: Boolean) {
        preferencesDataSource.setNotificationPermissionRequested(requested)
    }

    override fun getActiveViewRole(): String? = preferencesDataSource.getActiveViewRole()

    override fun setActiveViewRole(role: String) {
        preferencesDataSource.setActiveViewRole(role)
        activeViewRole.value = role
    }

    override fun observeActiveViewRole(): Flow<String?> = activeViewRole.asStateFlow()

    override fun setSubstitutionMode(mode: String) {
        preferencesDataSource.setSubstitutionMode(mode)
        substitutionMode.value = mode
    }

    override fun observeSubstitutionMode(): Flow<String?> = substitutionMode.asStateFlow()
}
