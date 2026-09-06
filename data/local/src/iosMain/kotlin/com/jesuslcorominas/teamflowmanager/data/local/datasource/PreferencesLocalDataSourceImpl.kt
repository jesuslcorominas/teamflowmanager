package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import platform.Foundation.NSUserDefaults

internal class PreferencesLocalDataSourceImpl : PreferencesDataSource {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun shouldShowInvalidSubstitutionAlert(): Boolean =
        if (defaults.objectForKey(KEY_SHOW_INVALID_SUBSTITUTION_ALERT) == null) {
            true
        } else {
            defaults.boolForKey(KEY_SHOW_INVALID_SUBSTITUTION_ALERT)
        }

    override fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean) {
        defaults.setBool(shouldShow, KEY_SHOW_INVALID_SUBSTITUTION_ALERT)
    }

    override fun getDefaultCaptainId(): String? {
        return defaults.stringForKey(KEY_DEFAULT_CAPTAIN_ID)
    }

    override fun setDefaultCaptainId(playerId: String?) {
        if (playerId == null) {
            defaults.removeObjectForKey(KEY_DEFAULT_CAPTAIN_ID)
        } else {
            defaults.setObject(playerId, KEY_DEFAULT_CAPTAIN_ID)
        }
    }

    override fun hasNotificationPermissionBeenRequested(): Boolean = defaults.boolForKey(KEY_NOTIFICATION_PERMISSION_REQUESTED)

    override fun setNotificationPermissionRequested(requested: Boolean) {
        defaults.setBool(requested, KEY_NOTIFICATION_PERMISSION_REQUESTED)
    }

    override fun getActiveViewRole(): String? = defaults.stringForKey(KEY_ACTIVE_VIEW_ROLE)

    override fun setActiveViewRole(role: String) {
        defaults.setObject(role, KEY_ACTIVE_VIEW_ROLE)
    }

    companion object {
        private const val KEY_SHOW_INVALID_SUBSTITUTION_ALERT = "show_invalid_substitution_alert"
        private const val KEY_DEFAULT_CAPTAIN_ID = "default_captain_id"
        private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
        private const val KEY_ACTIVE_VIEW_ROLE = "active_view_role"
    }
}
