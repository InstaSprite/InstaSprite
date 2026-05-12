package com.instasprite.app.data.model

import com.instasprite.app.ui.theme.ThemeFlavour
import kotlinx.serialization.Serializable

@Serializable
data class SettingPreferences(
    val language: String = "en",
    val themeFlavour: ThemeFlavour = ThemeFlavour.MOCHA,
    val drawSetting: DrawSetting = DrawSetting()
)

@Serializable
data class DrawSetting(
    val isCursorMode: Boolean = false
)