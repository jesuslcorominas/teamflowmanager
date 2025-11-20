package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import kotlinx.coroutines.flow.Flow

class MainViewModel(
    private val getMatchByIdUseCase: GetMatchByIdUseCase
) : ViewModel() {

    fun getMatchById(matchId: Long): Flow<Match?> = getMatchByIdUseCase(matchId)

}
