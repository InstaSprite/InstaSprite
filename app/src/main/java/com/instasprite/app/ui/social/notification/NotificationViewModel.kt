package com.instasprite.app.ui.social.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.instasprite.app.data.repository.NotificationRepository
import com.instasprite.app.domain.model.GroupedNotificationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    val pagedNotifications: Flow<PagingData<GroupedNotificationData>> = notificationRepository
        .getPagedNotifications()
        .cachedIn(viewModelScope)

    fun markAsRead(notification: GroupedNotificationData) {
        if (notification.isRead) return

        viewModelScope.launch {
            if (notification.relatedEntityId != null) {
                notificationRepository.markGroupAsRead(
                    notification.type.name,
                    notification.relatedEntityId
                )
            } else {
                // Fallback for non-groupable types without entity ID
                notificationRepository.markAsRead(notification.id)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }
}
