package com.jesuslcorominas.teamflowmanager.ui.navigation

import android.content.Intent

sealed class PendingNavigation {
    data class DeepLink(val intent: Intent) : PendingNavigation()

    data class Match(val matchId: Long) : PendingNavigation()
}
