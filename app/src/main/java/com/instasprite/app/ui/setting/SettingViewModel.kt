package com.instasprite.app.ui.setting

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.ThemeFlavour
import com.instasprite.app.utils.AppSettings
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
    val showLanguageDialog: Boolean = false,
    val selectedLanguage: String = "",
    val supportedLocales: List<Pair<String, String>> = emptyList(),
    val languages: List<String> = emptyList(),
    val languageCodes: List<String> = emptyList(),
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
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

    fun setThemeFlavour(flavour: ThemeFlavour) {
        _uiState.value = _uiState.value.copy(themeFlavour = flavour)
        AppSettings.setThemeFlavour(context, flavour)
    }

    fun setAppFont(font: AppFont) {
        _uiState.value = _uiState.value.copy(appFont = font)
        AppSettings.setAppFont(context, font)
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
}