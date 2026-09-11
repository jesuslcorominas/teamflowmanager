package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingSubstitutionsDataSource
import platform.Foundation.NSUserDefaults

internal class PendingSubstitutionsLocalDataSourceImpl : PendingSubstitutionsDataSource {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getRaw(matchId: String): String? = defaults.stringForKey(keyFor(matchId))

    override fun setRaw(
        matchId: String,
        raw: String?,
    ) {
        val key = keyFor(matchId)
        if (raw == null) {
            defaults.removeObjectForKey(key)
        } else {
            defaults.setObject(raw, key)
        }
    }

    // NSUserDefaults is a single app-wide namespace, so the prefix carries the feature domain to
    // avoid colliding with PreferencesLocalDataSourceImpl's keys. The Android side gets the same
    // isolation from its own preferences file, which is why the two key prefixes differ.
    private fun keyFor(matchId: String): String = KEY_PREFIX + matchId

    companion object {
        private const val KEY_PREFIX = "teamflowmanager_pending_substitutions_"
    }
}
