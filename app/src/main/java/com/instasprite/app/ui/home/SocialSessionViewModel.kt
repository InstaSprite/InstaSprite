package com.instasprite.app.ui.home

import androidx.lifecycle.ViewModel
import com.instasprite.app.domain.session.SocialSessionManager
import com.instasprite.app.domain.session.SocialSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SocialSessionViewModel @Inject constructor(
    private val sessionManager: SocialSessionManager
) : ViewModel() {

    val sessionState: StateFlow<SocialSessionState> = sessionManager.sessionState

    fun logout() {
        sessionManager.logout()
    }
}