package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateClubUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateClubViewModel(
    private val createClubUseCase: CreateClubUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _clubName = MutableStateFlow("")
    val clubName: StateFlow<String> = _clubName.asStateFlow()

    private val _clubNameError = MutableStateFlow<Int?>(null)
    val clubNameError: StateFlow<Int?> = _clubNameError.asStateFlow()

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Success(val club: Club) : UiState
        data class Error(val message: String) : UiState
    }

    fun onClubNameChanged(name: String) {
        _clubName.value = name
        _clubNameError.value = null
    }

    fun createClub() {
        val name = _clubName.value.trim()
        
        // Validate club name
        when {
            name.isEmpty() -> {
                _clubNameError.value = com.jesuslcorominas.teamflowmanager.R.string.club_name_error_empty
                return
            }
            name.length < 3 -> {
                _clubNameError.value = com.jesuslcorominas.teamflowmanager.R.string.club_name_error_too_short
                return
            }
            name.length > 50 -> {
                _clubNameError.value = com.jesuslcorominas.teamflowmanager.R.string.club_name_error_too_long
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val club = createClubUseCase(name)
                
                // Track club creation
                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_CREATED,
                    mapOf(
                        AnalyticsParam.CLUB_ID to club.id.toString(),
                        AnalyticsParam.CLUB_NAME to club.name
                    )
                )
                
                _uiState.value = UiState.Success(club)
            } catch (e: Exception) {
                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_CREATION_ERROR,
                    mapOf(
                        AnalyticsParam.ERROR_MESSAGE to (e.message ?: "Unknown error")
                    )
                )
                _uiState.value = UiState.Error(e.message ?: "Failed to create club")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
