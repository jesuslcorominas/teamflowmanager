package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ClubNameError {
    EMPTY_NAME,
    NAME_TOO_SHORT,
    NAME_TOO_LONG,
}

private const val MIN_CLUB_NAME_LENGTH = 3
private const val MAX_CLUB_NAME_LENGTH = 50

class CreateClubViewModel(
    private val createClubUseCase: CreateClubUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
    private val isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _clubName = MutableStateFlow("")
    val clubName: StateFlow<String> = _clubName.asStateFlow()

    private val _clubNameError = MutableStateFlow<ClubNameError?>(null)
    val clubNameError: StateFlow<ClubNameError?> = _clubNameError.asStateFlow()

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

        when {
            name.isEmpty() -> {
                _clubNameError.value = ClubNameError.EMPTY_NAME
                return
            }
            name.length < MIN_CLUB_NAME_LENGTH -> {
                _clubNameError.value = ClubNameError.NAME_TOO_SHORT
                return
            }
            name.length > MAX_CLUB_NAME_LENGTH -> {
                _clubNameError.value = ClubNameError.NAME_TOO_LONG
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val club = createClubUseCase(name)

                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_CREATED,
                    mapOf(
                        AnalyticsParam.CLUB_ID to club.id.toString(),
                        AnalyticsParam.CLUB_NAME to club.name,
                    ),
                )

                // Sync FCM token with new club subscription (fire-and-forget)
                syncFcmTokenAfterClubChange(club.firestoreId)

                _uiState.value = UiState.Success(club)
            } catch (e: Exception) {
                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_CREATION_ERROR,
                    mapOf(
                        AnalyticsParam.ERROR_MESSAGE to (e.message ?: "Unknown error"),
                    ),
                )
                _uiState.value = UiState.Error(e.message ?: "Failed to create club")
            }
        }
    }

    private fun syncFcmTokenAfterClubChange(clubFirestoreId: String?) {
        if (!isNotificationPermissionGranted()) return
        viewModelScope.launch {
            val user = getCurrentUser().first() ?: return@launch
            runCatching { syncFcmTokenUseCase(user.id, "android", clubFirestoreId) }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
