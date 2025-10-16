package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesLocalDataSource

internal class PreferencesLocalDataSourceImpl(
    context: Context,
) : PreferencesLocalDataSource {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    override fun shouldShowInvalidSubstitutionAlert(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_INVALID_SUBSTITUTION_ALERT, true)
    }
    
    override fun setShouldShowInvalidSubstitutionAlert(shouldShow: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_INVALID_SUBSTITUTION_ALERT, shouldShow)
            .apply()
    }
    
    override fun getDefaultCaptainId(): Long? {
        val captainId = sharedPreferences.getLong(KEY_DEFAULT_CAPTAIN_ID, -1L)
        return if (captainId == -1L) null else captainId
    }
    
    override fun setDefaultCaptainId(playerId: Long?) {
        sharedPreferences.edit()
            .putLong(KEY_DEFAULT_CAPTAIN_ID, playerId ?: -1L)
            .apply()
    }
    
    companion object {
        private const val PREFS_NAME = "teamflowmanager_preferences"
        private const val KEY_SHOW_INVALID_SUBSTITUTION_ALERT = "show_invalid_substitution_alert"
        private const val KEY_DEFAULT_CAPTAIN_ID = "default_captain_id"
    }
}
