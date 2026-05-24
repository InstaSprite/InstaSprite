package com.instasprite.app.ui.social.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.domain.model.NotificationData
import com.instasprite.app.data.network.model.toDomain
import com.instasprite.app.data.repository.NotificationRepository
import com.instasprite.app.domain.model.GroupedNotificationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<GroupedNotificationData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val hasMore: Boolean = true
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(refresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (!refresh && !_uiState.value.hasMore) return

        val nextPage = if (refresh) 0 else _uiState.value.page

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            notificationRepository.getGroupedNotifications(page = nextPage, size = 20)
                .onSuccess { pageDto ->
                    _uiState.update { state ->
                        val currentList = if (refresh) emptyList() else state.notifications
                        state.copy(
                            notifications = currentList + pageDto.content.map { it.toDomain() },
                            isLoading = false,
                            page = nextPage + 1,
                            hasMore = !pageDto.last
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    fun markAsRead(notification: GroupedNotificationData) {
        if (notification.isRead) return
        
        viewModelScope.launch {
            val result = if (notification.relatedEntityId != null) {
                notificationRepository.markGroupAsRead(notification.type.name, notification.relatedEntityId)
            } else {
                // Fallback for non-groupable types without entity ID
                notificationRepository.markAsRead(notification.id)
            }
            
            result.onSuccess {
                _uiState.update { state ->
                    val updatedList = state.notifications.map { notif ->
                        if (notif.id == notification.id) notif.copy(isRead = true) else notif
                    }
                    state.copy(notifications = updatedList)
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = notificationRepository.markAllAsRead()
            result.onSuccess {
                _uiState.update { state ->
                    val updatedList = state.notifications.map { it.copy(isRead = true) }
                    state.copy(notifications = updatedList)
                }
            }
        }
    }
}
