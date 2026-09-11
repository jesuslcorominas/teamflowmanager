package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingSubstitutionsDataSource

internal class PendingSubstitutionsLocalDataSourceImpl(
    context: Context,
) : PendingSubstitutionsDataSource {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getRaw(matchId: String): String? = sharedPreferences.getString(keyFor(matchId), null)

    override fun setRaw(
        matchId: String,
        raw: String?,
    ) {
        val editor = sharedPreferences.edit()
        if (raw == null) {
            editor.remove(keyFor(matchId))
        } else {
            editor.putString(keyFor(matchId), raw)
        }
        editor.apply()
    }

    private fun keyFor(matchId: String): String = KEY_PREFIX + matchId

    companion object {
        private const val PREFS_NAME = "teamflowmanager_pending_substitutions"
        private const val KEY_PREFIX = "pending_substitutions_"
    }
}
