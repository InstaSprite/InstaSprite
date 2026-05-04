package com.olaz.instasprite.ui.social.auth.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.domain.model.LoginRequest
import com.olaz.instasprite.domain.model.RegisterRequest
import com.olaz.instasprite.ui.social.auth.contract.AuthMode
import com.olaz.instasprite.ui.social.auth.contract.GoogleAuthUiState

@Composable
fun AuthForm(
    onLoginClick: (LoginRequest) -> Unit,
    onRegisterClick: (RegisterRequest) -> Unit,
    onSwitchToRegister: () -> Unit,
    onSwitchToLogin: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    uiState: GoogleAuthUiState,
) {

    LocalContext.current

    AnimatedContent(
        targetState = uiState.authMode,
        label = "AuthTransition"
    ) { mode ->
        when (mode) {
            AuthMode.LOGIN -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoginForm(
                        enabled = !uiState.isLoading,
                        onRegisterClick = onSwitchToRegister,
                        onForgotPasswordClick = onForgotPasswordClick,
                        onLogin = onLoginClick,
                    )

                }
            }

            AuthMode.REGISTER ->
                RegisterForm(
                    enabled = !uiState.isLoading,
                    onLoginClick = onSwitchToLogin,
                    onRegister = onRegisterClick
                )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}
