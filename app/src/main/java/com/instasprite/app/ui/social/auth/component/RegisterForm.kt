package com.instasprite.app.ui.social.auth.component

import android.annotation.SuppressLint
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.data.model.InputField
import com.instasprite.app.domain.model.RegisterRequest
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.theme.AppTheme


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun RegisterForm(
    enabled: Boolean = true,
    onRegister: (RegisterRequest) -> Unit = {},
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordVisualTransformation =
        if (isPasswordVisible) VisualTransformation.None
        else PasswordVisualTransformation('*')

    val confirmPasswordVisualTransformation =
        if (isConfirmPasswordVisible) VisualTransformation.None
        else PasswordVisualTransformation('*')

    val nameInputTextField = remember {
        InputField(
            label = context.getString(R.string.full_name),
            placeholder = "Instasprite",
            validator = { string ->
                string.isNotBlank()
            },
            errorMessage = context.getString(R.string.email_validation_error)
        )
    }

    val usernameInputTextField = remember {
        InputField(
            label = context.getString(R.string.username),
            placeholder = "instasprite789",
            validator = { string ->
                string.isNotBlank()
            },
            errorMessage = context.getString(R.string.email_validation_error)
        )
    }

    val emailInputTextField = remember {
        InputField(
            label = context.getString(R.string.email),
            placeholder = "exampl@domain.com",
            keyboardType = KeyboardType.Email,
            validator = { string ->
                Patterns.EMAIL_ADDRESS.matcher(string).matches()
            },
            errorMessage = context.getString(R.string.email_validation_error)
        )
    }

    val passwordInputTextField = remember {
        InputField(
            label = context.getString(R.string.password),
            validator = { password ->
                password.length >= 6 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() }
            },
            keyboardType = KeyboardType.Password,
            errorMessage = context.getString(R.string.password_validation_error)
        )
    }

    val confirmPasswordInputTextField = remember {
        InputField(
            label = "Confirm Password",
            validator = { password -> password == confirmPassword },
            keyboardType = KeyboardType.Password,
            errorMessage = context.getString(R.string.confirm_password_validation_error)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        InputTextField(
            enabled = enabled,
            value = name,
            onValueChange = { v ->
                name = v
            },
            imeAction = ImeAction.Next,
            inputField = nameInputTextField
        )

        InputTextField(
            enabled = enabled,
            value = username,
            onValueChange = { v ->
                username = v
            },
            imeAction = ImeAction.Next,
            inputField = usernameInputTextField
        )

        InputTextField(
            enabled = enabled,
            value = email,
            onValueChange = { v ->
                email = v
            },
            imeAction = ImeAction.Next,
            inputField = emailInputTextField
        )

        InputTextField(
            enabled = enabled,
            value = password,
            onValueChange = { v ->
                password = v
            },
            imeAction = ImeAction.Next,
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
            visualTransformation = passwordVisualTransformation
        )

        InputTextField(
            enabled = enabled,
            value = confirmPassword,
            onValueChange = { v ->
                confirmPassword = v
            },
            imeAction = ImeAction.Done,
            inputField = confirmPasswordInputTextField,
            trailingIcon = {
                IconToggleButton(
                    checked = isConfirmPasswordVisible,
                    colors = IconButtonDefaults.iconToggleButtonColors().copy(
                        checkedContentColor = AppTheme.colors.LinkColor,
                        contentColor = AppTheme.colors.InactiveColor,
                    ),
                    onCheckedChange = { isConfirmPasswordVisible = it },
                ) {
                    if (isConfirmPasswordVisible) Icon(Icons.Default.Visibility, null)
                    else Icon(Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = confirmPasswordVisualTransformation
        )

        Button(
            enabled = enabled
                    && (emailInputTextField.validator(email)
                    && passwordInputTextField.validator(password)
                    && confirmPasswordInputTextField.validator(confirmPassword)
                    && nameInputTextField.validator(name)
                    && usernameInputTextField.validator(username)),
            onClick = {
                onRegister(
                    RegisterRequest(
                        username = username,
                        name = name,
                        email = email,
                        password = password
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.SelectedColor,
                contentColor = AppTheme.colors.TextColorLight,
                disabledContainerColor = AppTheme.colors.Foreground1Color,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_in),
                color = if (enabled) AppTheme.colors.TextColorDark else AppTheme.colors.TextColorLight,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier.height(1.dp))

        Row(
            modifier = Modifier.clickable(true, onClick = onLoginClick),
        ) {
            Text(
                text = stringResource(R.string.already_has_account) + ' ',
                color = AppTheme.colors.TextColorLight,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.login),
                color = AppTheme.colors.AccentButtonColor,
                style = MaterialTheme.typography.labelLarge
            )
        }

    }
}

@Preview
@Composable
private fun Preview() {

    RegisterForm()
}