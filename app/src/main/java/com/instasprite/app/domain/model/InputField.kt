package com.instasprite.app.domain.model

import androidx.compose.ui.text.input.KeyboardType

data class InputField(
    val label: String,
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Companion.Text,
    val suffix: String? = null,
    val validator: (String) -> Boolean = { true },
    val errorMessage: String = "",
    var defaultValue: String = ""
)