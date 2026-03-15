package com.olaz.instasprite.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingPreferences(
    val language: String = "en",
    val isDarkMode: Boolean = false
)