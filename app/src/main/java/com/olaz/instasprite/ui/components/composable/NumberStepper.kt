package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme


@Composable
fun NumberStepper(
    value: Int,
    changeAmount: Int = 1,
    onValueChange: (Int) -> Unit,
    label: String,
    range: IntRange = 1..1024
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            color = CatppuccinUI.TextColorLight,
            style = CatppuccinTypography.bodyMedium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val newVal = (value - changeAmount).coerceIn(range)
                    textValue = newVal.toString()
                    onValueChange(newVal)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle,
                    contentDescription = "Decrease",
                    tint = CatppuccinUI.CurrentPalette.Blue
                )
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { str ->
                    textValue = str
                    str.toIntOrNull()?.let { v ->
                        if (v in range) onValueChange(v)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = CatppuccinUI.OutlineTextFieldColors.colors(),
                modifier = Modifier.width(80.dp),
                textStyle = CatppuccinTypography.bodyMedium,
                singleLine = true
            )

            IconButton(
                onClick = {
                    val newVal = (value + changeAmount).coerceIn(range)
                    textValue = newVal.toString()
                    onValueChange(newVal)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Increase",
                    tint = CatppuccinUI.CurrentPalette.Blue
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme {
        NumberStepper(
            value = 16,
            onValueChange = {},
            label = "test tes alo",
            changeAmount = 16,
            range = 1..512
        )
    }
}