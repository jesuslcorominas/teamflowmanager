package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.AccountExistsWithDifferentProviderException
import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResolvePendingCoachAssignmentsForUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithAppleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithGoogleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
    private val isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val resolvePendingCoachAssignments: ResolvePendingCoachAssignmentsForUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // Held between signIn success and permission result to complete the FCM sync
    private var pendingFcmSync: PendingFcmSync? = null

    private data class PendingFcmSync(
        val userId: String,
        val platform: String,
        val clubRemoteId: String?,
    )

    sealed interface UiState {
        data object Idle : UiState

        data object Loading : UiState

        data object Success : UiState

        data class Error(val message: String) : UiState

        /**
         * The identity's email already has an account created with another
         * provider. The screen tells the user to sign in with that one.
         */
        data object AccountExistsWithOtherProvider : UiState
    }

    sealed interface UiEvent {
        data object RequestNotificationPermission : UiEvent
    }

    fun signInWithGoogle(idToken: String) {
        signIn(method = "google") { signInWithGoogleUseCase(idToken) }
    }

    /** iOS only: Android never shows the Sign in with Apple button. */
    fun signInWithApple(credential: AppleSignInCredential) {
        signIn(method = "apple") { signInWithAppleUseCase(credential) }
    }

    private fun signIn(
        method: String,
        block: suspend () -> Result<User>,
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            block()
                .onSuccess { user ->
                    analyticsTracker.setUserId(user.id)
                    analyticsTracker.logEvent(
                        "login",
                        mapOf(
                            "method" to method,
                            "is_new_user" to "unknown",
                        ),
                    )
                    handlePostLogin(user)
                }
                .onFailure { exception ->
                    analyticsTracker.logEvent(
                        "login_error",
                        mapOf(
                            "method" to method,
                            "error" to (exception.message ?: "Unknown error"),
                        ),
                    )
                    _uiState.value =
                        if (exception is AccountExistsWithDifferentProviderException) {
                            UiState.AccountExistsWithOtherProvider
                        } else {
                            UiState.Error(exception.message ?: "Error al iniciar sesión")
                        }
                }
        }
    }

    private suspend fun handlePostLogin(user: User) {
        val platform = "android"
        // clubRemoteId is unknown at login time; subscription will be updated
        // when the user's club is loaded (e.g. in SplashViewModel / MainViewModel).
        // For now we sync the token without subscribing to a topic, which also
        // handles cleanup of any previous user's token on this device.
        val clubRemoteId: String? = null

        // Resolve any pending coach assignments for this user (non-blocking)
        viewModelScope.launch {
            runCatching {
                user.email?.let { resolvePendingCoachAssignments(user.id, it) }
            }
        }

        if (isNotificationPermissionGranted()) {
            runCatching {
                syncFcmTokenUseCase(user.id, platform, clubRemoteId)
            }
            _uiState.value = UiState.Success
        } else {
            // Store pending sync and defer navigation until the user responds to
            // the permission dialog — otherwise the screen navigates away before
            // the launcher can receive the result.
            pendingFcmSync = PendingFcmSync(user.id, platform, clubRemoteId)
            _events.emit(UiEvent.RequestNotificationPermission)
            // Success is set inside onNotificationPermissionResult
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        val pending = pendingFcmSync
        pendingFcmSync = null
        if (granted && pending != null) {
            viewModelScope.launch {
                runCatching {
                    syncFcmTokenUseCase(pending.userId, pending.platform, pending.clubRemoteId)
                }
            }
        }
        // Navigate regardless of whether permission was granted or denied
        _uiState.value = UiState.Success
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
