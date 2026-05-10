package com.olaz.instasprite.utils

import android.content.Context
import android.content.res.Configuration
import com.olaz.instasprite.data.model.DrawSetting
import com.olaz.instasprite.data.model.SettingPreferences
import com.olaz.instasprite.di.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

object AppSettings {

    fun onAttach(context: Context): Context {
        val settings = getPersistedSettings(context)

        applyTheme(settings.isDarkMode)

        return updateResources(context, settings.language)
    }

    fun getLanguage(context: Context): String {
        return getPersistedSettings(context).language
    }

    fun isDarkMode(context: Context): Boolean {
        return getPersistedSettings(context).isDarkMode
    }

    fun getDrawSetting(context: Context): DrawSetting {
        return getPersistedSettings(context).drawSetting
    }

    fun setLanguage(context: Context, language: String): Context {
        persist(context) { it.copy(language = language) }
        return updateResources(context, language)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        persist(context) { it.copy(isDarkMode = isDark) }
        applyTheme(isDark)
    }

    fun setDrawSetting(context: Context, setting: DrawSetting) {
        persist(context) { it.copy(drawSetting = setting) }
    }

    fun setCursorMode(context: Context, isCursorMode: Boolean) {
        persist(context) { it.copy(drawSetting = it.drawSetting.copy(isCursorMode = isCursorMode)) }
    }

    fun getSupportedLocales(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "vi" to "Tiếng Việt"
        )
    }

    private fun getPersistedSettings(context: Context): SettingPreferences = runBlocking {
        context.settingsDataStore.data.first()
    }

    private fun persist(context: Context, transform: (SettingPreferences) -> SettingPreferences) = runBlocking {
        context.settingsDataStore.updateData(transform)
    }

    private fun applyTheme(isDark: Boolean) {

    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}