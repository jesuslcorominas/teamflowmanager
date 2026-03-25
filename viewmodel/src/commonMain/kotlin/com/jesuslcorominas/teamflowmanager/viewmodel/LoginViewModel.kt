package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Idle : UiState

        data object Loading : UiState

        data object Success : UiState

        data class Error(val message: String) : UiState
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            signInWithGoogleUseCase(idToken)
                .onSuccess { user ->
                    analyticsTracker.setUserId(user.id)
                    analyticsTracker.logEvent(
                        "login",
                        mapOf(
                            "method" to "google",
                            "is_new_user" to "unknown",
                        ),
                    )

                    _uiState.value = UiState.Success
                }
                .onFailure { exception ->
                    analyticsTracker.logEvent(
                        "login_error",
                        mapOf(
                            "method" to "google",
                            "error" to (exception.message ?: "Unknown error"),
                        ),
                    )
                    _uiState.value = UiState.Error(exception.message ?: "Error al iniciar sesión")
                }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
