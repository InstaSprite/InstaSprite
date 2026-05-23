package com.instasprite.app.ui.components.composable

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme


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
            color = AppTheme.colors.TextColorLight,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val newVal = (value - changeAmount).coerceIn(range)
                    textValue = newVal.toString()
                    onValueChange(newVal)
                }
            ) {
                PixelIcon(
                    icon = R.drawable.ic_minus_border,
                    contentDescription = stringResource(R.string.decrease),
                    tint = AppTheme.colors.LinkColor
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
                colors = AppTheme.colors.outlineTextFieldColors(),
                modifier = Modifier.width(80.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true
            )

            IconButton(
                onClick = {
                    val newVal = (value + changeAmount).coerceIn(range)
                    textValue = newVal.toString()
                    onValueChange(newVal)
                }
            ) {
                PixelIcon(
                    icon = R.drawable.ic_plus_border,
                    contentDescription = stringResource(R.string.increase),
                    tint = AppTheme.colors.LinkColor
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