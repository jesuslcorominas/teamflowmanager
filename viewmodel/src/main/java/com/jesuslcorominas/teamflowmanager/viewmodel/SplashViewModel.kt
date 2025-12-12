package com.jesuslcorominas.teamflowmanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasLocalDataWithoutUserIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getTeam: GetTeamUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val hasLocalDataWithoutUserId: HasLocalDataWithoutUserIdUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState

        data object NotAuthenticated : UiState

        data object LocalDataNeedsAuth : UiState

        data object NoTeam : UiState

        data object TeamExists : UiState
    }

    init {
        performStartupTasks()
    }

    private fun performStartupTasks() {
        viewModelScope.launch {
            // Synchronize time with server on app startup
            try {
                synchronizeTimeUseCase()
                Log.d(TAG, "Time synchronized successfully on splash")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to synchronize time on splash", e)
                // Continue anyway - time sync will be attempted again when starting matches
            }

            // Continue with authentication checks
            checkLocalDataAndAuth()
        }
    }

    private suspend fun checkLocalDataAndAuth() {
        // Check for local data without user ID
        try {
            val hasLocalData = hasLocalDataWithoutUserId()
            if (hasLocalData) {
                Log.i(TAG, "Local data without user ID detected. Team exists without coachId.")
                // Check if user is authenticated
                val user = getCurrentUser().first()
                if (user == null) {
                    // Force authentication when local data exists but user is not authenticated
                    _uiState.value = UiState.LocalDataNeedsAuth
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for local data without user ID", e)
        }

        // Continue with authentication check
        checkAuthAndLoadTeam()
    }

    private suspend fun checkAuthAndLoadTeam() {
        val user = getCurrentUser().first()
        if (user == null) {
            _uiState.value = UiState.NotAuthenticated
        } else {
            loadTeam()
        }
    }

    private suspend fun loadTeam() {
        getTeam().collect { team ->
            if (team == null) {
                _uiState.value = UiState.NoTeam
            } else {
                _uiState.value = UiState.TeamExists
            }
        }
    }

    companion object {
        private const val TAG = "SplashViewModel"
    }
}
