package com.instasprite.app.ui.social.auth.dialog

import com.instasprite.app.utils.pixelDp

import android.annotation.SuppressLint
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.data.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.social.auth.contract.ForgotPasswordUiState
import com.instasprite.app.ui.theme.AppTheme


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ForgotPasswordDialog(
    forgotPasswordState: ForgotPasswordUiState,
    onDismiss: () -> Unit = {},
    onForgotPassword: (String) -> Unit = {},
    onResetPassword: (String, String) -> Unit = { _, _ -> }
) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var temporaryPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isTemporaryPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val showResetForm = forgotPasswordState.showResetForm
    val isSendingEmail = forgotPasswordState.isSendingEmail
    val isResettingPassword = forgotPasswordState.isResettingPassword
    val errorMessage = forgotPasswordState.errorMessage

    LaunchedEffect(showResetForm) {
        if (showResetForm) {
            Toast.makeText(
                context,
                context.getString(R.string.reset_password_email),
                Toast.LENGTH_SHORT
            ).show()
        }
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

    val temporaryPasswordInputTextField = remember {
        InputField(
            label = "Temporary Password",
            keyboardType = KeyboardType.Password,
            validator = { string -> string.isNotBlank() },
            errorMessage = "Please enter temporary password"
        )
    }

    val newPasswordInputTextField = remember {
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
            label = context.getString(R.string.confirm_password),
            validator = { password -> password == newPassword },
            keyboardType = KeyboardType.Password,
            errorMessage = context.getString(R.string.confirm_password_validation_error)
        )
    }

    CustomDialog(
        title = if (showResetForm) stringResource(R.string.reset_password) else stringResource(
            R.string.reset_password
        ),
        onDismiss = {
            if (!isSendingEmail && !isResettingPassword) onDismiss()
        },
        onConfirm = {
            if (!showResetForm && !isSendingEmail) {
                if (emailInputTextField.validator(email)) {
                    onForgotPassword(email)
                }
            } else if (showResetForm && !isResettingPassword) {
                if (temporaryPasswordInputTextField.validator(temporaryPassword) &&
                    newPasswordInputTextField.validator(newPassword) &&
                    confirmPasswordInputTextField.validator(confirmPassword)
                ) {
                    onResetPassword(temporaryPassword, newPassword)
                }
            }
        },
        confirmButtonText = if (showResetForm) stringResource(R.string.reset_password) else stringResource(
            R.string.send
        ),
        dismissButtonText = stringResource(R.string.cancel)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isSendingEmail || isResettingPassword) {
                CircularProgressIndicator(
                    modifier = Modifier.size(42.pixelDp),
                    color = AppTheme.colors.WarningColor,
                    strokeWidth = 2.pixelDp
                )
                Spacer(modifier = Modifier.height(10.pixelDp))
                Text(
                    text = if (isSendingEmail) stringResource(R.string.sending_email) else "Resetting password…",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (showResetForm) {
                InputTextField(
                    enabled = true,
                    value = temporaryPassword,
                    onValueChange = { v -> temporaryPassword = v },
                    imeAction = ImeAction.Next,
                    inputField = temporaryPasswordInputTextField,
                    trailingIcon = {
                        IconToggleButton(
                            checked = isTemporaryPasswordVisible,
                            colors = IconButtonDefaults.iconToggleButtonColors().copy(
                                checkedContentColor = AppTheme.colors.LinkColor,
                                contentColor = AppTheme.colors.InactiveColor,
                            ),
                            onCheckedChange = { isTemporaryPasswordVisible = it },
                        ) {
                            PixelIcon(
                                icon = if (isTemporaryPasswordVisible)
                                    R.drawable.ic_visible_on
                                else
                                    R.drawable.ic_visible_off,
                                tint = AppTheme.colors.TextColorLight,
                            )
                        }
                    },
                    visualTransformation = if (isTemporaryPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(
                        '*'
                    )
                )

                Spacer(modifier = Modifier.height(6.pixelDp))

                InputTextField(
                    enabled = true,
                    value = newPassword,
                    onValueChange = { v -> newPassword = v },
                    imeAction = ImeAction.Next,
                    inputField = newPasswordInputTextField,
                    trailingIcon = {
                        IconToggleButton(
                            checked = isNewPasswordVisible,
                            colors = IconButtonDefaults.iconToggleButtonColors().copy(
                                checkedContentColor = AppTheme.colors.LinkColor,
                                contentColor = AppTheme.colors.InactiveColor,
                            ),
                            onCheckedChange = { isNewPasswordVisible = it },
                        ) {
                            PixelIcon(
                                icon = if (isNewPasswordVisible)
                                    R.drawable.ic_visible_on
                                else
                                    R.drawable.ic_visible_off,
                                tint = AppTheme.colors.TextColorLight,
                            )
                        }
                    },
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(
                        '*'
                    )
                )

                Spacer(modifier = Modifier.height(6.pixelDp))

                InputTextField(
                    enabled = true,
                    value = confirmPassword,
                    onValueChange = { v -> confirmPassword = v },
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
                            PixelIcon(
                                icon = if (isConfirmPasswordVisible)
                                    R.drawable.ic_visible_on
                                else
                                    R.drawable.ic_visible_off,
                                tint = AppTheme.colors.TextColorLight,
                            )
                        }
                    },
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(
                        '*'
                    )
                )
            } else {
                InputTextField(
                    enabled = true,
                    value = email,
                    onValueChange = { v -> email = v },
                    imeAction = ImeAction.Done,
                    inputField = emailInputTextField
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.pixelDp))
                Text(
                    text = errorMessage,
                    color = AppTheme.colors.DismissButtonColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
