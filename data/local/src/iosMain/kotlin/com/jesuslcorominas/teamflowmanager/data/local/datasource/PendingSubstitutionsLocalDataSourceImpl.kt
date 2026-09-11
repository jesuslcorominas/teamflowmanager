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

    // NSUserDefaults es un espacio de nombres único por app: el prefijo incluye el dominio de la
    // feature para no colisionar con las claves de PreferencesLocalDataSourceImpl.
    private fun keyFor(matchId: String): String = KEY_PREFIX + matchId

    companion object {
        private const val KEY_PREFIX = "teamflowmanager_pending_substitutions_"
    }
}
