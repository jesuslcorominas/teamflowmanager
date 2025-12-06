package com.jesuslcorominas.teamflowmanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.HasLocalDataWithoutUserIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getTeam: GetTeamUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val hasLocalDataWithoutUserId: HasLocalDataWithoutUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState

        data object NotAuthenticated : UiState

        data object NoTeam : UiState

        data object TeamExists : UiState
    }

    init {
        checkLocalDataAndAuth()
    }

    private fun checkLocalDataAndAuth() {
        viewModelScope.launch {
            // Check for local data without user ID
            try {
                val hasLocalData = hasLocalDataWithoutUserId()
                if (hasLocalData) {
                    Log.i(TAG, "Local data without user ID detected. Team exists without coachId.")
                } else {
                    Log.d(TAG, "No local data without user ID found.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for local data without user ID", e)
            }

            // Continue with authentication check
            checkAuthAndLoadTeam()
        }
    }

    private fun checkAuthAndLoadTeam() {
        viewModelScope.launch {
            val user = getCurrentUser().first()
            if (user == null) {
                _uiState.value = UiState.NotAuthenticated
            } else {
                loadTeam()
            }
        }
    }

    private fun loadTeam() {
        viewModelScope.launch {
            getTeam().collect { team ->
                if (team == null) {
                    _uiState.value = UiState.NoTeam
                } else {
                    _uiState.value = UiState.TeamExists
                }
            }
        }
    }

    companion object {
        private const val TAG = "SplashViewModel"
    }
}
