package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SetNotificationPermissionRequestedUseCase
import kotlinx.coroutines.flow.Flow

class MainViewModel(
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
    private val hasNotificationPermissionBeenRequestedUseCase: HasNotificationPermissionBeenRequestedUseCase,
    private val setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase,
) : ViewModel() {

    fun getMatchById(matchId: Long): Flow<Match?> = getMatchByIdUseCase(matchId)

    fun hasNotificationPermissionBeenRequested(): Boolean =
        hasNotificationPermissionBeenRequestedUseCase()

    fun setNotificationPermissionRequested(requested: Boolean) {
        setNotificationPermissionRequestedUseCase(requested)
    }
}
