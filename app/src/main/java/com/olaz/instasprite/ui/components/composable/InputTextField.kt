package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.data.model.InputField
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun InputTextField(
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    inputField: InputField,
    trailingIcon: @Composable (() -> Unit)? = null,
    maxLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default,
    imeOptions: ImeOptions = ImeOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {

    val focusManager = LocalFocusManager.current
    var hasTyped by remember { mutableStateOf(false) }

    OutlinedTextField(
        enabled = enabled,
        value = value,
        onValueChange = {
            hasTyped = true
            onValueChange(it)
        },
        label = {
            Text(
                inputField.label, color = AppTheme.colors.SelectedColor,
                style = MaterialTheme.typography.bodySmall
            )
        },
        placeholder = {
            if (inputField.placeholder.isNotBlank())
                Text(
                    inputField.placeholder, color = AppTheme.colors.Subtext0Color,
                    style = MaterialTheme.typography.bodyMedium
                )
        },
        trailingIcon = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.End),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                inputField.suffix?.let {
                    Text(
                        it,
                        color = AppTheme.colors.LinkColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                trailingIcon?.invoke()
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = inputField.keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (imeAction == ImeAction.Next) {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            },
            onDone = {
                if (imeAction == ImeAction.Done) {
                    focusManager.clearFocus()
                }
            }
        ),
        singleLine = maxLines == 1,
        maxLines = maxLines,
        isError = hasTyped && !inputField.validator(value),
        supportingText = {
            if (hasTyped && !inputField.validator(value)) {
                Text(
                    inputField.errorMessage, color = AppTheme.colors.DismissButtonColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        colors = AppTheme.colors.outlineTextFieldColors(),
        modifier = modifier,
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Preview
@Composable
private fun Preview() {
    Column {
        InputTextField(
            value = "",
            onValueChange = {},
            inputField = InputField(
                label = "Label",
                placeholder = "Test"
            ),
        )
        InputTextField(
            value = "A",
            onValueChange = {},
            inputField = InputField(
                label = "Label",
                placeholder = "Test"
            ),
        )
        InputTextField(
            value = "A",
            onValueChange = {},
            inputField = InputField(
                label = "Label",
                placeholder = "Test",
                suffix = "Suffix"
            ),
        )
        InputTextField(
            value = "A",
            onValueChange = {},
            inputField = InputField(
                label = "Label",
                placeholder = "Test",
            ),
            trailingIcon = {
                Icon(Icons.Default.Preview, contentDescription = "Preview")
            }
        )
        InputTextField(
            value = "A",
            onValueChange = {},
            inputField = InputField(
                label = "Label",
                placeholder = "Test",
                suffix = "Suffix"
            ),
            trailingIcon = {
                Icon(Icons.Default.Preview, contentDescription = "Preview")
            }
        )
    }
}