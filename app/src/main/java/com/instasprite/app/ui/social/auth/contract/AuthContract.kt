package com.instasprite.app.ui.social.auth.contract

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.instasprite.app.data.model.AccountPreferences
import com.instasprite.app.data.network.model.GoogleLoginRequestDto
import com.instasprite.app.domain.model.Jwt
import com.instasprite.app.domain.model.LoginRequest
import com.instasprite.app.domain.model.RegisterRequest

enum class AuthMode { LOGIN, REGISTER }

data class GoogleAuthUiState(
    val isLoading: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val errorMessage: String? = null,
    val jwt: Jwt? = null,
    val isFirstTime: Boolean = false,
    val authMode: AuthMode = AuthMode.LOGIN,
    val showOtpDialog: Boolean = false,
    val pendingLoginRequest: LoginRequest? = null,
    val pendingGoogleLoginRequest: GoogleLoginRequestDto? = null,
    val otpError: String? = null
)

data class ForgotPasswordUiState(
    val showDialog: Boolean = false,
    val isSendingEmail: Boolean = false,
    val emailSent: Boolean = false,
    val showResetForm: Boolean = false,
    val isResettingPassword: Boolean = false,
    val isPasswordResetSuccess: Boolean = false,
    val errorMessage: String? = null
)

enum class AuthContentScreenState {
    ACCOUNT_LIST,
    AUTH_FORM,
}

data class AuthContentState(
    val uiState: GoogleAuthUiState = GoogleAuthUiState(),
    val forgotPasswordState: ForgotPasswordUiState = ForgotPasswordUiState(),
    val accounts: List<AccountPreferences> = emptyList(),
    val currentScreenState: AuthContentScreenState = AuthContentScreenState.AUTH_FORM,
    val selectedAccountForLogin: AccountPreferences? = null,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class AuthScreenEvent(
    val onLoginSuccess: () -> Unit,
    val onDismissSavedAccountLogin: () -> Unit,
    val onLoginWithSavedAccount: (LoginRequest) -> Unit,
    val onDismissOtpDialog: () -> Unit,
    val onVerifyOtp: (String) -> Unit,
    val onDismissForgotPasswordDialog: () -> Unit,
    val onForgotPassword: (String) -> Unit,
    val onResetPassword: (String, String) -> Unit,
    val onLogin: (LoginRequest) -> Unit,
    val onRegister: (RegisterRequest) -> Unit,
    val onSwitchToRegister: () -> Unit,
    val onSwitchToLogin: () -> Unit,
    val onShowForgotPasswordDialog: () -> Unit,
    val onShowAccountList: () -> Unit,
    val onShowAuthForm: () -> Unit,
    val onSelectAccount: (AccountPreferences) -> Unit,
    val onRemoveAccount: (String) -> Unit,
    val onGoogleSignInResultCancelled: (String) -> Unit,
    val onResetRegisterSuccess: () -> Unit,
    val onGoogleSignInClick: (
        Activity,
        ManagedActivityResultLauncher<Intent, ActivityResult>?,
        (GoogleLoginRequestDto) -> Unit,
        (String) -> Unit,
    ) -> Unit,
)
