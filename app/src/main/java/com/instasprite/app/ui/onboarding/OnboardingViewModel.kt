package com.instasprite.app.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.ThemeFlavour
import com.instasprite.app.utils.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingUiState(
    val themeFlavour: ThemeFlavour = ThemeFlavour.MOCHA,
    val appFont: AppFont = AppFont.DETERMINATION,
    val isCursorMode: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            themeFlavour = AppSettings.getThemeFlavour(context),
            appFont = AppSettings.getAppFont(context),
            isCursorMode = AppSettings.getDrawSetting(context).isCursorMode
        )
    }

    fun setThemeFlavour(flavour: ThemeFlavour) {
        _uiState.value = _uiState.value.copy(themeFlavour = flavour)
        AppSettings.setThemeFlavour(context, flavour)
    }

    fun setAppFont(font: AppFont) {
        _uiState.value = _uiState.value.copy(appFont = font)
        AppSettings.setAppFont(context, font)
    }

    fun setCursorMode(isCursorMode: Boolean) {
        _uiState.value = _uiState.value.copy(isCursorMode = isCursorMode)
        AppSettings.setCursorMode(context, isCursorMode)
    }

    fun finishOnboarding() {
        AppSettings.setHasSeenOnboarding(context, true)
    }
}
