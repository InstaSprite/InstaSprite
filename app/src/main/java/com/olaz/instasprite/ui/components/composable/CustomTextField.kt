package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun CustomTextField(
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    maxLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onFocusChange: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        enabled = enabled,
        value = value,
        onValueChange = { newValue ->
            onValueChange(newValue)
            onFocusChange?.invoke()
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
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
        maxLines = maxLines,
        singleLine = maxLines == 1,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.SelectedColor,
            unfocusedBorderColor = AppTheme.colors.TextColorLight.copy(alpha = 0.3f),
            focusedLabelColor = AppTheme.colors.BottomBarColor,
            unfocusedLabelColor = AppTheme.colors.TextColorLight.copy(alpha = 0.7f),
            focusedTextColor = AppTheme.colors.TextColorLight,
            unfocusedTextColor = AppTheme.colors.TextColorLight,
            cursorColor = AppTheme.colors.TextColorLight,
        ),
        modifier = modifier
    )
}
