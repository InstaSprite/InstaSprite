package com.instasprite.app.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

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
    val resources = LocalResources.current

    var isPasswordVisible by remember { mutableStateOf(false) }

    val visualTransformation =
        if (isPasswordVisible) VisualTransformation.None
        else PasswordVisualTransformation('*')

    val configuration = LocalConfiguration.current
    val passwordInputTextField = remember(configuration, errorText) {
        InputField(
            label = resources.getString(R.string.password),
            validator = { password ->
                password.length >= 6 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() }
            },
            keyboardType = KeyboardType.Password,
            errorMessage = errorText ?: resources.getString(R.string.password_validation_error)
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
        confirmButtonColor = AppTheme.colors.green,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.set_password_description),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(10.pixelDp))

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
                            checkedContentColor = AppTheme.colors.blue,
                            contentColor = AppTheme.colors.textDisabled,
                        ),
                        onCheckedChange = { isPasswordVisible = it },
                    ) {
                        PixelIcon(
                            icon = if (isPasswordVisible) R.drawable.ic_visible_on else R.drawable.ic_visible_off,
                            tint = AppTheme.colors.onSurface,
                        )
                    }
                },
                visualTransformation = visualTransformation
            )
        }
    }
}
