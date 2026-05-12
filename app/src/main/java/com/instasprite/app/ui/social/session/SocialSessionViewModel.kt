package com.instasprite.app.ui.social.session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.instasprite.app.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SocialSessionViewModel @Inject constructor(
    private val sessionManager: SocialSessionManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val sessionState: StateFlow<SocialSessionState> = sessionManager.sessionState

    val isLoggedIn: StateFlow<Boolean> = sessionState
        .map { it is SocialSessionState.LoggedIn }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            sessionManager.isLoggedIn()
        )

    fun logout() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.deleteFcmToken(token)
            } catch (e: Exception) {
                Log.w("SocialSessionViewModel", "Failed to delete FCM token", e)
            } finally {
                sessionManager.onLogout()
            }
        }
    }
}
