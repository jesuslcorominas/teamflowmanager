package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePresidentNotificationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPresidentNotificationsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUnreadPresidentNotificationsCountUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsReadUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsUnreadUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PresidentNotificationsViewModel(
    private val getNotifications: GetPresidentNotificationsUseCase,
    private val getUnreadCount: GetUnreadPresidentNotificationsCountUseCase,
    private val markAsRead: MarkPresidentNotificationAsReadUseCase,
    private val markAsUnread: MarkPresidentNotificationAsUnreadUseCase,
    private val deleteNotification: DeletePresidentNotificationUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
) : ViewModel() {
    sealed interface UiState {
        data object Loading : UiState

        data class Success(val notifications: List<PresidentNotification>) : UiState

        data object Error : UiState

        data object NoClubMembership : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        load()
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val clubId = currentClubId() ?: return@launch
            try {
                markAsRead(clubId, notificationId)
            } catch (_: Exception) {
                // Silently ignore — UI state will be refreshed by snapshot listener
            }
        }
    }

    fun markAsUnread(notificationId: String) {
        viewModelScope.launch {
            val clubId = currentClubId() ?: return@launch
            try {
                markAsUnread(clubId, notificationId)
            } catch (_: Exception) {
                // Silently ignore — UI state will be refreshed by snapshot listener
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val clubId = currentClubId() ?: return@launch
            try {
                deleteNotification(clubId, notificationId)
            } catch (_: Exception) {
                // Silently ignore — UI state will be refreshed by snapshot listener
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val membership =
                try {
                    getUserClubMembership().first()
                } catch (_: Exception) {
                    null
                }

            val clubId = membership?.clubFirestoreId
            if (clubId.isNullOrBlank()) {
                _uiState.value = UiState.NoClubMembership
                return@launch
            }

            launch {
                try {
                    getNotifications(clubId).collect { notifications ->
                        _uiState.value = UiState.Success(notifications)
                    }
                } catch (_: Exception) {
                    _uiState.value = UiState.Error
                }
            }

            launch {
                try {
                    getUnreadCount(clubId).collect { count ->
                        _unreadCount.value = count
                    }
                } catch (_: Exception) {
                    _unreadCount.value = 0
                }
            }
        }
    }

    private suspend fun currentClubId(): String? {
        val membership =
            try {
                getUserClubMembership().first()
            } catch (_: Exception) {
                null
            }
        return membership?.clubFirestoreId?.takeIf { it.isNotBlank() }
    }
}
