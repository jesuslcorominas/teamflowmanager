package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


enum class InvitationCodeError {
    EMPTY_CODE,
    CODE_TOO_SHORT,
    CODE_TOO_LONG,
    INVALID_FORMAT
}

private const val MIN_INVITATION_CODE_LENGTH = 6
private const val MAX_INVITATION_CODE_LENGTH = 10

class JoinClubViewModel(
    private val joinClubByCodeUseCase: JoinClubByCodeUseCase,
    private val analyticsTracker: AnalyticsTracker
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
        // Only allow alphanumeric characters
        val filtered = code.filter { it.isLetterOrDigit() }.uppercase()
        _invitationCode.value = filtered
        _invitationCodeError.value = null
    }

    fun joinClub() {
        val code = _invitationCode.value.trim()

        // Validate invitation code
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

                // Track club join
                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_JOINED,
                    mapOf(
                        AnalyticsParam.CLUB_ID to result.club.id.toString(),
                        AnalyticsParam.CLUB_NAME to result.club.name,
                        AnalyticsParam.INVITATION_CODE to code,
                        AnalyticsParam.HAS_ORPHAN_TEAM to (result.orphanTeam != null).toString()
                    )
                )

                // Track orphan team linkage if applicable
                if (result.orphanTeam != null) {
                    analyticsTracker.logEvent(
                        AnalyticsEvent.ORPHAN_TEAM_LINKED,
                        mapOf(
                            AnalyticsParam.TEAM_ID to result.orphanTeam.id.toString(),
                            AnalyticsParam.TEAM_NAME to result.orphanTeam.name,
                            AnalyticsParam.CLUB_ID to result.club.id.toString()
                        )
                    )
                }

                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                analyticsTracker.logEvent(
                    AnalyticsEvent.CLUB_JOIN_ERROR,
                    mapOf(
                        AnalyticsParam.ERROR_MESSAGE to (e.message ?: "Unknown error"),
                        AnalyticsParam.INVITATION_CODE to code
                    )
                )
                _uiState.value = UiState.Error(e.message ?: "Failed to join club")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
