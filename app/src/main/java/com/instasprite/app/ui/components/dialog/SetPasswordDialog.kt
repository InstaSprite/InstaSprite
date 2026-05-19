package com.instasprite.app.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.data.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun SetPasswordDialog(
    enabled: Boolean = true,
    errorText: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    val colors = AppTheme.colors
    val context = LocalContext.current

    var isPasswordVisible by remember { mutableStateOf(false) }

    val visualTransformation =
        if (isPasswordVisible) VisualTransformation.None
        else PasswordVisualTransformation('*')

    val passwordInputTextField = remember {
        InputField(
            label = context.getString(R.string.password),
            validator = { password ->
                password.length >= 6 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() }
            },
            keyboardType = KeyboardType.Password,
            errorMessage = errorText ?: context.getString(R.string.password_validation_error)
        )
    }

    CustomDialog(
        title = stringResource(R.string.set_password),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(password) },
        confirmButtonText = if (enabled)
            stringResource(R.string.confirm)
        else
            stringResource(R.string.setting_password),
        dismissButtonText = stringResource(R.string.cancel),
        confirmButtonColor = AppTheme.colors.AccentButtonColor,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.set_password_description),
                color = colors.Subtext0Color
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputTextField(
                enabled = enabled,
                value = password,
                onValueChange = { v ->
                    password = v
                },
                imeAction = ImeAction.Done,
                inputField = passwordInputTextField,
                trailingIcon = {
                    IconToggleButton(
                        checked = isPasswordVisible,
                        colors = IconButtonDefaults.iconToggleButtonColors().copy(
                            checkedContentColor = AppTheme.colors.LinkColor,
                            contentColor = AppTheme.colors.InactiveColor,
                        ),
                        onCheckedChange = { isPasswordVisible = it },
                    ) {
                        if (isPasswordVisible) Icon(Icons.Default.Visibility, null)
                        else Icon(Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = visualTransformation
            )
        }
    }
}
