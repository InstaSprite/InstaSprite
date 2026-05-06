package com.olaz.instasprite.ui.social.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.olaz.instasprite.data.model.AccountPreferences
import com.olaz.instasprite.data.model.AccountType
import com.olaz.instasprite.data.network.model.GoogleLoginRequestDto
import com.olaz.instasprite.data.repository.AccountRepository
import com.olaz.instasprite.data.repository.AuthRepository
import com.olaz.instasprite.data.repository.NotificationRepository
import com.olaz.instasprite.domain.model.Jwt
import com.olaz.instasprite.domain.model.LoginRequest
import com.olaz.instasprite.domain.model.RegisterRequest
import com.olaz.instasprite.ui.social.auth.contract.AuthContentState
import com.olaz.instasprite.ui.social.auth.contract.AuthMode
import com.olaz.instasprite.ui.social.auth.contract.ForgotPasswordUiState
import com.olaz.instasprite.ui.social.auth.contract.GoogleAuthUiState
import com.olaz.instasprite.ui.social.session.SocialSessionManager
import com.olaz.instasprite.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val notificationRepository: NotificationRepository,
    private val sessionManager: SocialSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoogleAuthUiState())
    val uiState: StateFlow<GoogleAuthUiState> = _uiState

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState

    private val _accounts = accountRepository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val contentState = combine(
        _uiState,
        _forgotPasswordState,
        _accounts
    ) { ui, forgot, accounts ->
        AuthContentState(
            uiState = ui,
            forgotPasswordState = forgot,
            accounts = accounts
        )
    }

    fun login(loginRequest: LoginRequest) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, otpError = null) }
        viewModelScope.launch {
            val result = authRepository.login(loginRequest)

            result.fold(
                onSuccess = { jwt: Jwt ->
                    sessionManager.onLoginSuccess(jwt)

                    accountRepository.addAccount(
                        AccountPreferences(
                            username = jwt.username!!,
                            name = jwt.name,
                            email = jwt.email,
                            accountType = AccountType.LOCAL
                        )
                    )

                    // Send FCM token to backend
                    sendFcmTokenToBackend()

                    _uiState.value = GoogleAuthUiState(
                        isLoading = false,
                        jwt = jwt,
                        isFirstTime = jwt.isFirstTime ?: false,
                        showOtpDialog = false,
                        pendingLoginRequest = null,
                        pendingGoogleLoginRequest = null
                    )
                },
                onFailure = { exception: Throwable ->
                    val errorMessage = exception.message ?: ""
                    // Check if error code is M013 (OTP required)
                    if (errorMessage.startsWith("M013:")) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showOtpDialog = true,
                                pendingLoginRequest = loginRequest,
                                errorMessage = null,
                                otpError = null
                            )
                        }
                    } else {
                        val userMsg = exception.toUserMessage(context)
                        // If OTP dialog is showing, show error in dialog
                        if (_uiState.value.showOtpDialog) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    otpError = userMsg
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = userMsg,
                                    showOtpDialog = false,
                                    pendingLoginRequest = null
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    fun verifyOtpAndLogin(otpCode: String) {
        val pendingRequest = _uiState.value.pendingLoginRequest
        val pendingGoogleRequest = _uiState.value.pendingGoogleLoginRequest

        if (pendingRequest != null) {
            val loginRequestWithOtp = pendingRequest.copy(otpCode = otpCode)
            login(loginRequestWithOtp)
        } else if (pendingGoogleRequest != null) {
            val googleRequestWithOtp = pendingGoogleRequest.copy(otpCode = otpCode)
            loginWithGoogleIdToken(googleRequestWithOtp)
        }
    }

    fun dismissOtpDialog() {
        _uiState.update {
            it.copy(
                showOtpDialog = false,
                pendingLoginRequest = null,
                pendingGoogleLoginRequest = null,
                otpError = null
            )
        }
    }

    fun loginWithGoogleIdToken(googleLoginRequestDto: GoogleLoginRequestDto) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, otpError = null) }
        viewModelScope.launch {

            val result = authRepository.loginWithGoogle(googleLoginRequestDto)

            result.fold(
                onSuccess = { jwt: Jwt ->
                    sessionManager.onLoginSuccess(jwt)

                    sendFcmTokenToBackend()

                    _uiState.value = GoogleAuthUiState(
                        isLoading = false,
                        jwt = jwt,
                        isFirstTime = jwt.isFirstTime ?: false,
                        showOtpDialog = false,
                        pendingLoginRequest = null,
                        pendingGoogleLoginRequest = null
                    )
                },
                onFailure = { exception: Throwable ->
                    val errorMessage = exception.message ?: ""
                    // Check if error code is M013 (OTP required)
                    if (errorMessage.startsWith("M013:")) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showOtpDialog = true,
                                pendingGoogleLoginRequest = googleLoginRequestDto,
                                errorMessage = null,
                                otpError = null
                            )
                        }
                    } else {
                        val userMsg = exception.toUserMessage(context)
                        // If OTP dialog is showing, show error in dialog
                        if (_uiState.value.showOtpDialog) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    otpError = userMsg
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = userMsg,
                                    showOtpDialog = false,
                                    pendingLoginRequest = null,
                                    pendingGoogleLoginRequest = null
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    fun register(registerRequest: RegisterRequest) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.register(registerRequest)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegisterSuccess = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { exception: Throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.toUserMessage(context)
                        )
                    }
                }
            )
        }
    }

    fun removeAccountFromList(username: String) {
        viewModelScope.launch {
            accountRepository.removeAccount(username)
        }
    }

    fun switchToRegister() {
        _uiState.update { it.copy(authMode = AuthMode.REGISTER) }
    }

    fun switchToLogin() {
        _uiState.update { it.copy(authMode = AuthMode.LOGIN) }
    }

    fun resetRegisterSuccess() {
        _uiState.update { it.copy(isRegisterSuccess = false) }
    }

    fun showForgotPasswordDialog() {
        _forgotPasswordState.value = ForgotPasswordUiState(showDialog = true)
    }

    fun dismissForgotPasswordDialog() {
        _forgotPasswordState.value = ForgotPasswordUiState()
    }

    fun forgotPassword(email: String) {
        _forgotPasswordState.update {
            it.copy(
                isSendingEmail = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            result.fold(
                onSuccess = {
                    _forgotPasswordState.update {
                        it.copy(
                            isSendingEmail = false,
                            emailSent = true,
                            showResetForm = true
                        )
                    }
                },
                onFailure = { exception ->
                    _forgotPasswordState.update {
                        it.copy(
                            isSendingEmail = false,
                            errorMessage = exception.toUserMessage(context)
                        )
                    }
                }
            )
        }
    }

    fun resetPassword(temporaryPassword: String, newPassword: String) {
        _forgotPasswordState.update {
            it.copy(
                isResettingPassword = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val result = authRepository.resetPassword(temporaryPassword, newPassword)
            result.fold(
                onSuccess = {
                    _forgotPasswordState.update {
                        it.copy(
                            isResettingPassword = false,
                            isPasswordResetSuccess = true
                        )
                    }
                    // Close dialog after a short delay to show success
                    delay(500)
                    _forgotPasswordState.value = ForgotPasswordUiState()
                },
                onFailure = { exception ->
                    _forgotPasswordState.update {
                        it.copy(
                            isResettingPassword = false,
                            errorMessage = exception.toUserMessage(context)
                        )
                    }
                }
            )
        }
    }

    fun clearForgotPasswordError() {
        _forgotPasswordState.update { it.copy(errorMessage = null) }
    }

    private fun sendFcmTokenToBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.registerFcmToken(token).fold(
                    onSuccess = {
                        Log.d("AuthViewModel", "FCM token registered successfully")
                    },
                    onFailure = { exception ->
                        Log.w("AuthViewModel", "Failed to register FCM token", exception)
                    }
                )
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Error getting FCM token", e)
            }
        }
    }
}
