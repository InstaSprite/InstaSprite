package com.olaz.instasprite.data.model

import androidx.compose.ui.text.input.KeyboardType

data class InputField(
    val label: String = "",
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    val suffix: String? = null,
    val validator: (String) -> Boolean = { true },
    val errorMessage: String = "",
    val defaultValue: String = ""
)