package com.instasprite.app.ui.setting

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.repository.AuthRepository
import com.instasprite.app.domain.session.SocialSessionManager
import com.instasprite.app.ui.theme.ThemeFlavour
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.utils.AppSettings
import com.instasprite.app.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingUiState(
    val themeFlavour: ThemeFlavour = ThemeFlavour.MOCHA,
    val appFont: AppFont = AppFont.DETERMINATION,
    val is2FAEnabled: Boolean = false,
    val isLoading2FAStatus: Boolean = true,
    val showLanguageDialog: Boolean = false,
    val showOtpEnrollmentDialog: Boolean = false,
    val showOtpInputDialog: Boolean = false,
    val showDisableOtpDialog: Boolean = false,
    val selectedLanguage: String = "",
    val supportedLocales: List<Pair<String, String>> = emptyList(),
    val languages: List<String> = emptyList(),
    val languageCodes: List<String> = emptyList(),
    val otpSecret: String = "",
    val otpQrCodeBase64: String = "",
    val otpAccountName: String = "",
    val otpIssuer: String = "",
    val isLoadingOtp: Boolean = false,
    val isEnabling2FA: Boolean = false,
    val isDisabling2FA: Boolean = false,
    val otpError: String? = null,
    val enable2FAError: String? = null,
    val disable2FAError: String? = null,
    val hasPassword: Boolean = true,
    val showSetPasswordDialog: Boolean = false,
    val isSettingPassword: Boolean = false,
    val setPasswordError: String? = null,
    val setPasswordSuccess: Boolean = false
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SocialSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        load2FAStatus()
        loadPasswordStatus()
    }

    private fun loadSettings() {
        val supportedLocales = AppSettings.getSupportedLocales()
        val languages = supportedLocales.map { it.second }
        val languageCodes = supportedLocales.map { it.first }
        
        val currentCode = AppSettings.getLanguage(context)
        val index = languageCodes.indexOf(currentCode)
        val selectedLanguage = if (index >= 0) languages[index] else ""
        
        _uiState.value = _uiState.value.copy(
            themeFlavour = AppSettings.getThemeFlavour(context),
            appFont = AppSettings.getAppFont(context),
            supportedLocales = supportedLocales,
            languages = languages,
            languageCodes = languageCodes,
            selectedLanguage = selectedLanguage
        )
    }

    private fun load2FAStatus() {
        viewModelScope.launch {
            val result = authRepository.get2FAStatus()
            result.fold(
                onSuccess = { enabled ->
                    _uiState.value = _uiState.value.copy(
                        is2FAEnabled = enabled,
                        isLoading2FAStatus = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading2FAStatus = false,
                        is2FAEnabled = false
                    )
                }
            )
        }
    }

    fun setThemeFlavour(flavour: ThemeFlavour) {
        _uiState.value = _uiState.value.copy(themeFlavour = flavour)
        AppSettings.setThemeFlavour(context, flavour)
    }

    fun setAppFont(font: AppFont) {
        _uiState.value = _uiState.value.copy(appFont = font)
        AppSettings.setAppFont(context, font)
    }

    fun toggle2FA(enabled: Boolean) {
        if (enabled) {
            enrollOtp()
        } else {
            showDisableOtpDialog()
        }
    }

    private fun enrollOtp() {
        _uiState.value = _uiState.value.copy(
            isLoadingOtp = true,
            otpError = null
        )
        
        viewModelScope.launch {
            val result = authRepository.enrollOtp()
            result.fold(
                onSuccess = { otpData ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingOtp = false,
                        showOtpEnrollmentDialog = true,
                        otpSecret = otpData.secret,
                        otpQrCodeBase64 = otpData.qrCodePngBase64,
                        otpAccountName = otpData.accountName,
                        otpIssuer = otpData.issuer,
                        is2FAEnabled = otpData.enabled
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingOtp = false,
                        otpError = error.toUserMessage(context),
                        is2FAEnabled = false
                    )
                }
            )
        }
    }

    fun dismissOtpEnrollmentDialog() {
        _uiState.value = _uiState.value.copy(showOtpEnrollmentDialog = false)
    }

    fun showOtpInputDialog() {
        _uiState.value = _uiState.value.copy(
            showOtpEnrollmentDialog = false,
            showOtpInputDialog = true,
            enable2FAError = null
        )
    }

    fun dismissOtpInputDialog() {
        _uiState.value = _uiState.value.copy(showOtpInputDialog = false)
    }

    fun verifyAndEnable2FA(otpCode: String) {
        _uiState.value = _uiState.value.copy(
            isEnabling2FA = true,
            enable2FAError = null
        )

        viewModelScope.launch {
            val result = authRepository.enable2FA(otpCode)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isEnabling2FA = false,
                        showOtpInputDialog = false,
                        is2FAEnabled = true
                    )
                    // Refresh status to ensure consistency
                    load2FAStatus()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isEnabling2FA = false,
                        enable2FAError = error.toUserMessage(context)
                    )
                }
            )
        }
    }

    fun showDisableOtpDialog() {
        _uiState.value = _uiState.value.copy(
            showDisableOtpDialog = true,
            disable2FAError = null
        )
    }

    fun dismissDisableOtpDialog() {
        _uiState.value = _uiState.value.copy(showDisableOtpDialog = false)
    }

    fun verifyAndDisable2FA(otpCode: String) {
        _uiState.value = _uiState.value.copy(
            isDisabling2FA = true,
            disable2FAError = null
        )

        viewModelScope.launch {
            val result = authRepository.disable2FA(otpCode)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isDisabling2FA = false,
                        showDisableOtpDialog = false,
                        is2FAEnabled = false
                    )
                    // Refresh status to ensure consistency
                    load2FAStatus()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isDisabling2FA = false,
                        disable2FAError = error.toUserMessage(context)
                    )
                }
            )
        }
    }

    fun showLanguageDialog() {
        _uiState.value = _uiState.value.copy(showLanguageDialog = true)
    }

    fun dismissLanguageDialog() {
        _uiState.value = _uiState.value.copy(showLanguageDialog = false)
    }

    fun selectLanguage(languageIndex: Int) {
        val languageCodes = _uiState.value.languageCodes
        if (languageIndex in languageCodes.indices) {
            val newLanguageCode = languageCodes[languageIndex]
            val languages = _uiState.value.languages
            val selectedLanguage = languages[languageIndex]
            
            _uiState.value = _uiState.value.copy(
                selectedLanguage = selectedLanguage,
                showLanguageDialog = false
            )
            
            restartActivityWithNewLocale(newLanguageCode)
        }
    }

    private fun restartActivityWithNewLocale(newLanguageCode: String) {
        AppSettings.setLanguage(context, newLanguageCode)

        viewModelScope.launch {
            delay(100)

            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent?.component

            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)

            Runtime.getRuntime().exit(0)
        }
    }

    // ── Set Password (Google-only accounts) ──────────────────

    private fun loadPasswordStatus() {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(hasPassword = user.hasPassword)
                }
            }
        }
    }

    fun showSetPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showSetPasswordDialog = true,
            setPasswordError = null,
            setPasswordSuccess = false
        )
    }

    fun dismissSetPasswordDialog() {
        _uiState.value = _uiState.value.copy(showSetPasswordDialog = false)
    }

    fun setPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            isSettingPassword = true,
            setPasswordError = null
        )

        viewModelScope.launch {
            val result = authRepository.setPassword(password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSettingPassword = false,
                        setPasswordSuccess = true,
                        hasPassword = true,
                        showSetPasswordDialog = false
                    )
                    // Refresh session to update hasPassword
                    sessionManager.refreshCurrentUser()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSettingPassword = false,
                        setPasswordError = error.toUserMessage(context)
                    )
                }
            )
        }
    }
}