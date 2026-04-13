package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubResult
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class InvitationCodeError {
    EMPTY_CODE,
    CODE_TOO_SHORT,
    CODE_TOO_LONG,
    INVALID_FORMAT,
}

private const val MIN_INVITATION_CODE_LENGTH = 6
private const val MAX_INVITATION_CODE_LENGTH = 10

class JoinClubViewModel(
    private val joinClubByCodeUseCase: JoinClubByCodeUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
    private val isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _invitationCode = MutableStateFlow("")
    val invitationCode: StateFlow<String> = _invitationCode.asStateFlow()

    private val _invitationCodeError = MutableStateFlow<InvitationCodeError?>(null)
    val invitationCodeError: StateFlow<InvitationCodeError?> = _invitationCodeError.asStateFlow()

    sealed interface UiState {
        data object Idle : UiState

        data object Loading : UiState

        data class Success(val result: JoinClubResult) : UiState

        data class Error(val message: String) : UiState
    }

    fun onInvitationCodeChanged(code: String) {
        val filtered = code.filter { it.isLetterOrDigit() }.uppercase()
        _invitationCode.value = filtered
        _invitationCodeError.value = null
    }

    fun joinClub() {
        val code = _invitationCode.value.trim()

        when {
            code.isEmpty() -> {
                _invitationCodeError.value = InvitationCodeError.EMPTY_CODE
                return
            }
            code.length < MIN_INVITATION_CODE_LENGTH -> {
                _invitationCodeError.value = InvitationCodeError.CODE_TOO_SHORT
                return
            }
            code.length > MAX_INVITATION_CODE_LENGTH -> {
                _invitationCodeError.value = InvitationCodeError.CODE_TOO_LONG
                return
            }
            !code.all { it.isLetterOrDigit() } -> {
                _invitationCodeError.value = InvitationCodeError.INVALID_FORMAT
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = joinClubByCodeUseCase(code)

                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_JOINED,
                    mapOf(
                        AnalyticsParam.CLUB_ID to result.club.id.toString(),
                        AnalyticsParam.CLUB_NAME to result.club.name,
                        AnalyticsParam.INVITATION_CODE to code,
                        AnalyticsParam.HAS_ORPHAN_TEAM to (result.orphanTeam != null).toString(),
                    ),
                )

                val orphanTeam = result.orphanTeam
                if (orphanTeam != null) {
                    analyticsTracker.logEvent(
                        AnalyticsEvent.ORPHAN_TEAM_LINKED,
                        mapOf(
                            AnalyticsParam.TEAM_ID to orphanTeam.id.toString(),
                            AnalyticsParam.TEAM_NAME to orphanTeam.name,
                            AnalyticsParam.CLUB_ID to result.club.id.toString(),
                        ),
                    )
                }

                // Sync FCM token with new club subscription (fire-and-forget)
                syncFcmTokenAfterClubChange(result.club.remoteId)

                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_JOIN_ERROR,
                    mapOf(
                        AnalyticsParam.ERROR_MESSAGE to (e.message ?: "Unknown error"),
                        AnalyticsParam.INVITATION_CODE to code,
                    ),
                )
                _uiState.value = UiState.Error(e.message ?: "Failed to join club")
            }
        }
    }

    private fun syncFcmTokenAfterClubChange(clubRemoteId: String?) {
        if (!isNotificationPermissionGranted()) return
        viewModelScope.launch {
            val user = getCurrentUser().first() ?: return@launch
            runCatching { syncFcmTokenUseCase(user.id, "android", clubRemoteId) }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
