package com.instasprite.app.utils

import android.content.Context
import com.instasprite.app.ui.theme.ThemeFlavour
import android.content.res.Configuration
import com.instasprite.app.data.model.DrawSetting
import com.instasprite.app.data.model.SettingPreferences
import com.instasprite.app.di.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

object AppSettings {

    fun onAttach(context: Context): Context {
        val settings = getPersistedSettings(context)
        return updateResources(context, settings.language)
    }

    fun getLanguage(context: Context): String {
        return getPersistedSettings(context).language
    }

    fun getThemeFlavour(context: Context): ThemeFlavour {
        return getPersistedSettings(context).themeFlavour
    }

    fun getDrawSetting(context: Context): DrawSetting {
        return getPersistedSettings(context).drawSetting
    }

    fun setLanguage(context: Context, language: String): Context {
        persist(context) { it.copy(language = language) }
        return updateResources(context, language)
    }

    fun setThemeFlavour(context: Context, flavour: ThemeFlavour) {
        persist(context) { it.copy(themeFlavour = flavour) }
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

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}