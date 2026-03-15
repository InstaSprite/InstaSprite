package com.olaz.instasprite.data.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthStateManager @Inject constructor(
    @ApplicationContext context: Context,
    private val tokenUtils: TokenManager
) {

    private val _isLoggedIn = MutableStateFlow(tokenUtils.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun getTokenManager(): TokenManager = tokenUtils

    fun updateAuthState() {
        _isLoggedIn.value = tokenUtils.isLoggedIn()
    }

    fun logout() {
        tokenUtils.clearTokens()
        _isLoggedIn.value = false
    }
}