package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import kotlinx.coroutines.flow.Flow

class MainViewModel(
    private val matchNotificationController: MatchNotificationController
) : ViewModel() {

    fun getMatchById(matchId: Long): Flow<Match?> {
        return matchNotificationController.getMatchById(matchId)
    }
}
