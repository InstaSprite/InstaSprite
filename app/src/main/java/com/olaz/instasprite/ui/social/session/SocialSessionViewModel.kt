package com.olaz.instasprite.ui.social.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SocialSessionViewModel @Inject constructor(
    private val sessionManager: SocialSessionManager
) : ViewModel() {

    val sessionState: StateFlow<SocialSessionState> = sessionManager.sessionState

    val isLoggedIn: StateFlow<Boolean> = sessionState
        .map { it is SocialSessionState.LoggedIn }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            sessionManager.isLoggedIn()
        )
}
